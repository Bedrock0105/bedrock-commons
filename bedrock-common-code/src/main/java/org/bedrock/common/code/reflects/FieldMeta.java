package org.bedrock.common.code.reflects;

import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import java.util.Objects;

/**
 * 字段元数据描述类
 * <p>用于统一描述字段的上下文信息（原生Field对象、所属类、字段类型、泛型类型、注解等），
 * 并通过缓存机制优化反射性能，提供便捷的字段元信息访问方法。</p>
 *
 * <p>核心特性：
 * <ul>
 * <li>1. 缓存反射获取的类型、注解、名称等信息，避免重复反射开销</li>
 * <li>2. 支持通过 {@link ResolvableType} 解析字段泛型类型，简化泛型处理</li>
 * <li>3. 线程安全：通过 volatile 修饰缓存字段，双重检查锁定保证初始化安全</li>
 * <li>4. 提供字段修饰符（static/final）、注解的便捷判断方法</li>
 * <li>5. 支持 Spring 合并注解解析（元注解、组合注解、@{@link org.springframework.core.annotation.AliasFor} 别名）</li>
 * </ul>
 *
 * @author Custom Implementation
 * @see Field
 * @see ResolvableType
 * @see org.springframework.core.MethodParameter
 * @since 1.0.0
 */
public class FieldMeta {

	/**
	 * 原生 {@link Field} 对象，当前类封装的核心目标（构造器保证非null）
	 */
	private final Field field;

	/**
	 * 字段所属的容器类（缓存）。
	 * 可通过 {@link #getContainingClass()} 延迟初始化，也可通过构造器直接指定；
	 * volatile 修饰保证多线程可见性
	 */
	private volatile Class<?> containingClass;

	/**
	 * 字段的类型（缓存），等价于 {@link Field#getType()}，避免重复反射；
	 * volatile 修饰保证多线程可见性
	 */
	private volatile Class<?> fieldType;

	/**
	 * 字段的泛型类型（缓存），等价于 {@link Field#getGenericType()}；
	 * volatile 修饰保证多线程可见性，null 表示无泛型
	 */
	@Nullable
	private volatile Type genericFieldType;

	/**
	 * 字段的可解析泛型类型（缓存），基于 Spring {@link ResolvableType} 封装，简化泛型解析；
	 * volatile 修饰保证多线程可见性
	 */
	private volatile ResolvableType resolvableType;

	/**
	 * 字段上的所有注解（缓存），等价于 {@link Field#getAnnotations()}；
	 * volatile 修饰保证多线程可见性，null 表示未初始化
	 */
	@Nullable
	private volatile Annotation[] fieldAnnotations;

	/**
	 * 字段名称（缓存），等价于 {@link Field#getName()}；
	 * volatile 修饰保证多线程可见性，补充 private 修饰符（原遗漏）
	 */
	@Nullable
	private volatile String fieldName;

	// ======================== 构造器 ========================

	/**
	 * 基础构造器：通过原生 {@link Field} 对象创建 {@link FieldMeta}
	 *
	 * @param field 原生字段对象，不可为 null
	 * @throws IllegalArgumentException 如果 field 为 null，抛出非法参数异常
	 */
	public FieldMeta(Field field) {
		Assert.notNull(field, "Field must not be null");
		this.field = field;
	}

	/**
	 * 反射构造器：通过「容器类 + 字段名」反射获取 Field 创建 {@link FieldMeta}
	 *
	 * @param containingClass 字段所属的容器类，不可为 null
	 * @param fieldName       字段名称，不可为空字符串
	 * @throws IllegalArgumentException 1. 容器类/字段名为空；2. 指定类中无此字段时抛出
	 */
	public FieldMeta(Class<?> containingClass, String fieldName) {
		Assert.notNull(containingClass, "containingClass must not be null");
		Assert.hasText(fieldName, "fieldName must not be empty");
		Field field = ReflectionUtils.findField(containingClass, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No such field [" + fieldName + "] in class [" + containingClass.getName() + "]");
		}
		this.field = field;
		this.containingClass = containingClass; // 提前赋值，避免后续懒加载
		this.fieldName = fieldName; // 提前赋值，避免后续懒加载
	}

	/**
	 * 拷贝构造器：基于已有 {@link FieldMeta} 实例创建新实例，复用所有缓存信息
	 *
	 * @param original 原始 {@link FieldMeta} 实例，不可为 null
	 * @throws IllegalArgumentException 如果 original 为 null，抛出非法参数异常
	 */
	public FieldMeta(FieldMeta original) {
		Assert.notNull(original, "Original FieldMeta must not be null");
		this.field = original.field;
		this.containingClass = original.containingClass;
		this.fieldType = original.fieldType;
		this.genericFieldType = original.genericFieldType;
		this.resolvableType = original.resolvableType;
		this.fieldAnnotations = original.fieldAnnotations;
		this.fieldName = original.fieldName;
	}

	// ======================== 静态工厂方法 ========================

	/**
	 * 静态工厂方法：通过原生 {@link Field} 创建 {@link FieldMeta}
	 * <p>语义更友好，替代直接 new 构造器</p>
	 *
	 * @param field 原生字段对象，不可为 null
	 * @return FieldMeta 实例
	 * @throws IllegalArgumentException 如果 field 为 null，抛出非法参数异常
	 */
	public static FieldMeta forField(Field field) {
		return new FieldMeta(field);
	}

	/**
	 * 静态工厂方法：通过「容器类 + 字段名」创建 {@link FieldMeta}
	 * <p>语义更友好，替代直接 new 构造器</p>
	 *
	 * @param containingClass 字段所属的容器类，不可为 null
	 * @param fieldName       字段名称，不可为空字符串
	 * @return FieldMeta 实例
	 * @throws IllegalArgumentException 1. 容器类/字段名为空；2. 指定类中无此字段时抛出
	 */
	public static FieldMeta forField(Class<?> containingClass, String fieldName) {
		return new FieldMeta(containingClass, fieldName);
	}

	// ======================== 基础信息获取 ========================

	/**
	 * 获取原生 {@link Field} 对象
	 *
	 * @return 原生字段对象，非 null
	 */
	public Field getField() {
		return field;
	}

	/**
	 * 获取字段所属的容器类（字段声明类）
	 * <p>优先返回缓存值，未初始化时通过 {@link Field#getDeclaringClass()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段所属的类，非 null
	 */
	public Class<?> getContainingClass() {
		Class<?> containingClass = this.containingClass;
		if (containingClass == null) {
			containingClass = field.getDeclaringClass();
			this.containingClass = containingClass;
		}
		return containingClass;
	}

	/**
	 * 获取字段名称
	 * <p>优先返回缓存值，未初始化时通过 {@link Field#getName()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段名称，非空字符串
	 */
	public String getFieldName() {
		String fieldName = this.fieldName;
		if (fieldName == null) {
			fieldName = field.getName();
			this.fieldName = fieldName;
		}
		return fieldName;
	}

	/**
	 * 获取字段的类型（非泛型）
	 * <p>优先返回缓存值，未初始化时通过 {@link Field#getType()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段的 Class 类型，非 null
	 */
	public Class<?> getFieldType() {
		Class<?> fieldType = this.fieldType;
		if (fieldType == null) {
			fieldType = field.getType();
			this.fieldType = fieldType;
		}
		return fieldType;
	}

	/**
	 * 获取字段的泛型类型
	 * <p>优先返回缓存值，未初始化时通过 {@link Field#getGenericType()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段的泛型 Type 对象，非 null（无泛型时返回 {@link Class} 实例）
	 */
	public Type getGenericFieldType() {
		Type genericFieldType = this.genericFieldType;
		if (genericFieldType == null) {
			genericFieldType = field.getGenericType();
			this.genericFieldType = genericFieldType;
		}
		return genericFieldType;
	}

	/**
	 * 获取字段的可解析泛型类型（Spring 封装）
	 * <p>基于 {@link ResolvableType} 简化泛型解析，优先返回缓存值，未初始化时创建并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段的 ResolvableType 对象，非 null
	 * @see ResolvableType#forField(Field)
	 */
	public ResolvableType getResolvableType() {
		ResolvableType resolvableType = this.resolvableType;
		if (resolvableType == null) {
			resolvableType = ResolvableType.forField(field);
			this.resolvableType = resolvableType;
		}
		return resolvableType;
	}

	/**
	 * 获取字段对应的 {@link Member} 实例（字段属于 Member 子类型）
	 *
	 * @return 字段的 Member 实例，非 null
	 */
	public Member getMember() {
		return this.field;
	}

	/**
	 * 获取字段的注解承载元素（即字段本身）
	 *
	 * @return 字段的 AnnotatedElement 实例，非 null
	 */
	public AnnotatedElement getAnnotatedElement() {
		return this.field;
	}

	// ======================== 注解相关 ========================

	/**
	 * 获取字段上的所有注解
	 * <p>优先返回缓存值，未初始化时通过 {@link Field#getAnnotations()} 解析并缓存；
	 * 双重检查锁定保证线程安全</p>
	 *
	 * @return 字段注解数组，空数组（而非 null）表示无注解
	 */
	public Annotation[] getFieldAnnotations() {
		Annotation[] annotations = fieldAnnotations;
		if (annotations == null) {
			annotations = field.getAnnotations();
			fieldAnnotations = annotations;
		}
		return annotations;
	}

	/**
	 * 获取字段上指定类型的注解（原生反射，不处理合并注解/元注解）
	 *
	 * @param annotationType 要获取的注解类型，不可为 null
	 * @param <A>            注解泛型类型
	 * @return 匹配的注解实例，无则返回 null
	 * @throws IllegalArgumentException 如果 annotationType 为 null，抛出非法参数异常
	 * @see AnnotatedElement#getAnnotation(Class)
	 */
	@Nullable
	public <A extends Annotation> A getFieldAnnotation(Class<A> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return getAnnotatedElement().getAnnotation(annotationType);
	}

	/**
	 * 判断字段上是否存在指定类型的注解（原生反射，不处理合并注解/元注解）
	 *
	 * @param annotationType 要判断的注解类型，不可为 null
	 * @param <A>            注解泛型类型
	 * @return true = 存在该注解，false = 不存在
	 * @throws IllegalArgumentException 如果 annotationType 为 null，抛出非法参数异常
	 * @see AnnotatedElement#isAnnotationPresent(Class)
	 */
	public <A extends Annotation> boolean hasFieldAnnotation(Class<A> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return getAnnotatedElement().isAnnotationPresent(annotationType);
	}

	/**
	 * 判断字段上是否存在指定合并注解（支持元注解、组合注解、@{@link org.springframework.core.annotation.AliasFor} 别名）
	 * <p>区别于 {@link #hasFieldAnnotation(Class)}：可解析注解上的元注解（如自定义注解标注的 @Component）</p>
	 *
	 * @param annotationType 注解类型，不可为 null
	 * @return true=存在（直接/元/组合注解），false=不存在
	 * @throws IllegalArgumentException 如果 annotationType 为 null，抛出非法参数异常
	 * @see AnnotatedElementUtils#isAnnotated(AnnotatedElement, Class)
	 */
	public boolean hasMergedAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return AnnotatedElementUtils.isAnnotated(field, annotationType);
	}

	/**
	 * 获取字段上的指定合并注解实例（支持元注解、组合注解、@{@link org.springframework.core.annotation.AliasFor} 别名）
	 * <p>区别于 {@link #getFieldAnnotation(Class)}：可解析注解上的元注解（如自定义注解标注的 @Component）</p>
	 *
	 * @param annotationType 注解类型，不可为 null
	 * @param <A>            注解泛型类型
	 * @return 合并后的注解实例，无则返回 null
	 * @throws IllegalArgumentException 如果 annotationType 为 null，抛出非法参数异常
	 * @see AnnotatedElementUtils#findMergedAnnotation(AnnotatedElement, Class)
	 */
	@Nullable
	public <A extends Annotation> A getMergedAnnotation(Class<A> annotationType) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		return AnnotatedElementUtils.findMergedAnnotation(field, annotationType);
	}

	// ======================== 修饰符相关 ========================

	/**
	 * 获取字段的修饰符（如 public/private/static/final 等）
	 *
	 * @return 字段修饰符的 int 常量，参考 {@link Modifier} 常量
	 * @see Field#getModifiers()
	 * @see Modifier
	 */
	public int getModifiers() {
		return field.getModifiers();
	}

	/**
	 * 判断字段是否为静态字段
	 *
	 * @return true = 静态字段（static），false = 实例字段
	 * @see Modifier#isStatic(int)
	 */
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	/**
	 * 判断字段是否为最终字段（不可修改）
	 *
	 * @return true = 最终字段（final），false = 非 final 字段
	 * @see Modifier#isFinal(int)
	 */
	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	// ======================== 通用方法 ========================

	/**
	 * 克隆当前 {@link FieldMeta} 实例（复用缓存信息）
	 * <p>重写 Object.clone()，返回类型为 {@link FieldMeta}，避免强制类型转换</p>
	 * <p>修正原注释笔误：FieldParameter → FieldMeta</p>
	 *
	 * @return 新的 FieldMeta 实例，与原实例内容一致
	 */
	@Override
	protected FieldMeta clone() {
		return new FieldMeta(this);
	}

	/**
	 * 基于原生 {@link Field} 实例判断相等性
	 *
	 * @param other 待比较对象
	 * @return true=相等（同一Field实例），false=不相等
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		FieldMeta fieldMeta = (FieldMeta) other;
		return Objects.equals(field, fieldMeta.field);
	}

	/**
	 * 基于原生 {@link Field} 实例生成哈希值
	 *
	 * @return 哈希值
	 */
	@Override
	public int hashCode() {
		return Objects.hash(field);
	}
}

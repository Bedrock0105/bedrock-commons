//package org.bedrock.common.http;
//
//import org.bedrock.common.http.annotation.*;
//import org.bedrock.common.http.enums.HttpMethod;
//import org.bedrock.common.http.util.HttpUtil;
//
//import java.util.Map;
//
//public class Test {
//
//	public static void main(String[] args) {
////		HttpConfiguration.getDefault().requestBodyLog(true);
//		Map<String, Object> data = Map.of("data", "KMIijKjbr9UwEuTWyaXXGILrQORuQ7/WRWzcGTpjvrAhAWCRcRI4xzb0IVj0JILzqoJHdIabBKFkbiPtiOS5V3l6QjhDzsWFvGbstYJyyiA+OKphkO6O3PIWtwWR88HVnkrGmPiO5LyvLeZj/C6YCnspfefZ56j16RhJArLJksU7pEgEPgEbQGwLsaZv8GbAI1hwXwl6cZdBAzJ+sZXw4DQWx4dmW8ASDv9ssnK2maY=");
//		Map<String, String> headers = Map.of("Blade-Auth", "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnRfaWQiOiIwMDAwMDAiLCJpc0ZhY2UiOjEsInVzZXJfbmFtZSI6ImFkbWluIiwicmVhbF9uYW1lIjoiIiwiYXZhdGFyIjoiIiwidHlwZSI6MSwiY2xpZW50X2lkIjoic2FiZXIiLCJyb2xlX25hbWUiOiJhZG1pbmlzdHJhdG9yIiwibGljZW5zZSI6InBvd2VyZWQgYnkgYmxhZGV4IiwicG9zdF9pZCI6IiIsInVzZXJfaWQiOiIxMTIzNTk4ODIxNzM4Njc1MjAxIiwicm9sZV9pZCI6IjE0MDIxNDc2OTQ0Mzg1NDMzNjIiLCJzY29wZSI6WyJhbGwiXSwibmlja19uYW1lIjoiYWRtaW4iLCJvYXV0aF9pZCI6IiIsImRldGFpbCI6eyJ0eXBlIjoid2ViIiwiaXNGYWNlIjoxLCJseF90eXBlIjowfSwiZXhwIjoxNzY5MDgwMDE1LCJkZXB0X2lkIjoiMTEyMzU5ODgxMzczODY3NTIwMSIsImp0aSI6ImJjYjgzNjA4LTBlYmYtNGQxMC1hMGVkLTVmYTQwYzA2M2YxMiIsImFjY291bnQiOiJhZG1pbiJ9.dIwnyLD5fFpuMeusj4IGm9d1kVuZlk_fPHoQIR9jWtM");
////		String result = HttpUtil.get("https://xl.cnelearning.cn/api/blade-opsm/channel/page", data, headers, String.class);
////		System.out.println(result);
////		HttpRequest request = HttpUtil.createRequest("https://xl.cnelearning.cn/api/blade-opsm/channel/page", HttpMethod.GET);
////		request.addHeaders(headers);
////		request.addQuery(data);
////		String result = request.execute(String.class);
////		System.out.println(result);
////		HttpConfiguration.getDefault().requestLog(true);
////		HttpConfiguration.getDefault().requestHeadersLog(false);
//		A client = HttpConfiguration.getDefault().getClient(A.class);
//		HttpResponse response = client.get(1L, "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnRfaWQiOiIwMDAwMDAiLCJpc0ZhY2UiOjEsInVzZXJfbmFtZSI6ImFkbWluIiwicmVhbF9uYW1lIjoiIiwiYXZhdGFyIjoiIiwidHlwZSI6MSwiY2xpZW50X2lkIjoic2FiZXIiLCJyb2xlX25hbWUiOiJhZG1pbmlzdHJhdG9yIiwibGljZW5zZSI6InBvd2VyZWQgYnkgYmxhZGV4IiwicG9zdF9pZCI6IiIsInVzZXJfaWQiOiIxMTIzNTk4ODIxNzM4Njc1MjAxIiwicm9sZV9pZCI6IjE0MDIxNDc2OTQ0Mzg1NDMzNjIiLCJzY29wZSI6WyJhbGwiXSwibmlja19uYW1lIjoiYWRtaW4iLCJvYXV0aF9pZCI6IiIsImRldGFpbCI6eyJ0eXBlIjoid2ViIiwiaXNGYWNlIjoxLCJseF90eXBlIjowfSwiZXhwIjoxNzY5MDgwMDE1LCJkZXB0X2lkIjoiMTEyMzU5ODgxMzczODY3NTIwMSIsImp0aSI6ImJjYjgzNjA4LTBlYmYtNGQxMC1hMGVkLTVmYTQwYzA2M2YxMiIsImFjY291bnQiOiJhZG1pbiJ9.dIwnyLD5fFpuMeusj4IGm9d1kVuZlk_fPHoQIR9jWtM", "KMIijKjbr9UwEuTWyaXXGILrQORuQ7/WRWzcGTpjvrAhAWCRcRI4xzb0IVj0JILzqoJHdIabBKFkbiPtiOS5V3l6QjhDzsWFvGbstYJyyiA+OKphkO6O3PIWtwWR88HVnkrGmPiO5LyvLeZj/C6YCnspfefZ56j16RhJArLJksU7pEgEPgEbQGwLsaZv8GbAI1hwXwl6cZdBAzJ+sZXw4DQWx4dmW8ASDv9ssnK2maY=");
//		response.close();
//		System.out.println(new String(response.getBytes()));
//	}
//
//	@BedrockClient(url = "https://xl.cnelearning.cn", basePath = "/api/blade-opsm",
//		headers = @Headers(value = {
//			@Header(name = "weeweccse", defaultValue = "application/json"),
//			@Header(name = "Accsdwefwrgrfrept", defaultValue = "application/json")
//		}))
//	interface A {
//
//		@GetRequest(url = "/channel/page",
//			headers = @Headers(value = {
//				@Header(name = "weewecddddcse", defaultValue = "application/json"),
//				@Header(name = "Accsdwefdssdsdsdwrgrfrept", defaultValue = "application/json")
//			}),
//			disableSslValidation = "true")
//		@Headers({
//			@Header(name = "Blade-Auth", defaultValue = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnRfaWQiOiIwMDAwMDAiLCJpc0ZhY2UiOjEsInVzZXJfbmFtZSI6ImFkbWluIiwicmVhbF9uYW1lIjoiIiwiYXZhdGFyIjoiIiwidHlwZSI6MSwiY2xpZW50X2lkIjoic2FiZXIiLCJyb2xlX25hbWUiOiJhZG1pbmlzdHJhdG9yIiwibGljZW5zZSI6InBvd2VyZWQgYnkgYmxhZGV4IiwicG9zdF9pZCI6IiIsInVzZXJfaWQiOiIxMTIzNTk4ODIxNzM4Njc1MjAxIiwicm9sZV9pZCI6IjE0MDIxNDc2OTQ0Mzg1NDMzNjIiLCJzY29wZSI6WyJhbGwiXSwibmlja19uYW1lIjoiYWRtaW4iLCJvYXV0aF9pZCI6IiIsImRldGFpbCI6eyJ0eXBlIjoid2ViIiwiaXNGYWNlIjoxLCJseF90eXBlIjowfSwiZXhwIjoxNzY4MDU0MTIyLCJkZXB0X2lkIjoiMTEyMzU5ODgxMzczODY3NTIwMSIsImp0aSI6IjA2NmVhMWIwLWI3ZDMtNDhlOS1iZjQwLTY0ODc1MGQ4NGIwZCIsImFjY291bnQiOiJhZG1pbiJ9.QEG55fTjZXp4RhQWk3Px5Zc2n9ma_ei0M7OhWf-t950"),
//			@Header(name = "Accsdwdssdsddsdsddsefwrgrfrept", defaultValue = "application/json")
//		})
//		@Retry
//		@HttpProxy(host = "127.0.0.1", port = 8888)
//		@LogConf(requestBodyLog = true, responseBodyLog = true)
//		HttpResponse get(@Param("id") Long id, @Header(name = "Blade-Auth") String name, @Body("data") Object body);
//	}
//}

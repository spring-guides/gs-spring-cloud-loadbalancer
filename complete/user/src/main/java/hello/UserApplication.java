package hello;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Olga Maciaszek-Sharma
 */
@SpringBootApplication
@RestController
public class UserApplication {

	private final WebClient.Builder loadBalancedWebClientBuilder;
	private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

	public UserApplication(WebClient.Builder webClientBuilder,
			ReactorLoadBalancerExchangeFilterFunction lbFunction) {
		this.loadBalancedWebClientBuilder = webClientBuilder;
		this.lbFunction = lbFunction;
	}

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

	@RequestMapping("/hi")
	public Mono<String> hi(@RequestParam(value = "name", defaultValue = "Mary") String name) {
		return loadBalancedWebClientBuilder.build().get().uri("http://say-hello/greeting")
				.retrieve().bodyToMono(String.class)
				.map(greeting -> String.format("%s, %s!", greeting, name));
	}

	@RequestMapping("/hello")
	public Mono<String> hello(@RequestParam(value = "name", defaultValue = "John") String name) {
		return WebClient.builder()
				.filter(lbFunction)
				.build().get().uri("http://say-hello/greeting")
				.retrieve().bodyToMono(String.class)
				.map(greeting -> String.format("%s, %s!", greeting, name));
	}
}


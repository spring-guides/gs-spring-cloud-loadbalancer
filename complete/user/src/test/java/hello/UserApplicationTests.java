package hello;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApplicationTests {

	private ConfigurableApplicationContext application1;
	private ConfigurableApplicationContext application2;
	private ConfigurableApplicationContext application3;
	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate testRestTemplate;

	@BeforeEach
	void startApps() {
		application1 = startApp(8090);
		application2 = startApp(9092);
		application3 = startApp(9999);
	}

	@AfterEach
	void closeApps() {
		application1.close();
		application2.close();
		application3.close();
	}

	@Test
	void shouldRoundRobinOverInstancesWhenCallingServicesViaLoadBalancer() {
		ResponseEntity<String> response1 = testRestTemplate
				.getForEntity("http://localhost:" + port + "/hi", String.class);
		ResponseEntity<String> response2 = testRestTemplate
				.getForEntity("http://localhost:" + port + "/hi", String.class);
		ResponseEntity<String> response3 = testRestTemplate
				.getForEntity("http://localhost:" + port + "/hello", String.class);

		then(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
		then(response1.getBody()).isEqualTo("1, Mary!");
		then(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
		then(response2.getBody()).isEqualTo("2, Mary!");
		then(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
		then(response3.getBody()).isEqualTo("3, John!");
	}

	private ConfigurableApplicationContext startApp(int port) {
		return SpringApplication.run(TestApplication.class,
				"--server.port=" + port,
				"--spring.jmx.enabled=false");
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	static class TestApplication {

		static AtomicInteger atomicInteger = new AtomicInteger();

		@RequestMapping(value = "/greeting")
		public Integer greet() {
			return atomicInteger.incrementAndGet();
		}

		@RequestMapping(value = "/")
		public String health() {
			return "ok";
		}
	}
}

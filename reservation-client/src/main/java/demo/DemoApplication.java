package demo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableZuulProxy
@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
public class DemoApplication {


    @Bean
    CommandLineRunner dc(DiscoveryClient dc) {
        return args ->
                dc.getInstances("reservation-service")
                        .forEach(si -> System.out.println(String.format("(%s) %s:%s", si.getServiceId(), si.getHost(), si.getPort())));
    }

    @Bean
    CommandLineRunner rt(RestTemplate rt) {
        return args -> {

            List<HttpMessageConverter<?>> messageConverters = rt.getMessageConverters();
            messageConverters.forEach(x -> System.out.println(x));

            ParameterizedTypeReference<Resources<Reservation>> ptr = new ParameterizedTypeReference<Resources<Reservation>>() {
            };
            ResponseEntity<Resources<Reservation>> re = rt.exchange(
                    "http://reservation-service/reservations",
                    HttpMethod.GET,
                    null,
                    ptr);
            System.out.println(re.getHeaders().getContentType().toString());
            System.out.println(ToStringBuilder.reflectionToString(re.getBody()));
            re.getBody().getContent().forEach(System.out::println);

        };
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
/*

@FeignClient("reservation-service")
interface ReservationClient {

    @RequestMapping(method = RequestMethod.GET, value = "/reservations")
    Collection<Reservation> getReservations();
}
*/

class Reservation {
    private Long id;
    private String reservationName;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("id=").append(id);
        sb.append(", reservationName='").append(reservationName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}
/*

@Component
class ReservationIntegration {

    @Autowired
    private ReservationClient reservationClient;

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    @HystrixCommand (fallbackMethod = "getReservationNamesFallback")
    public Collection<String> getReservationNames() {
        return this.reservationClient.getReservations()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }

}

@RestController
class ReservationNameController {

    @Autowired
    private ReservationIntegration reservationIntegration;

    @RequestMapping("/names")
    Collection<String> reservationNames() {
        return this.reservationIntegration.getReservationNames();
    }

}*/

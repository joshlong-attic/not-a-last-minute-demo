package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableZuulProxy
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

            ParameterizedTypeReference<Resources<Reservation>> ptr =
                    new ParameterizedTypeReference<Resources<Reservation>>() {
                    };

            ResponseEntity<Resources<Reservation>> re = rt.exchange(
                    RequestEntity.get(URI.create("http://reservation-service/reservations"))
                            .accept(MediaTypes.HAL_JSON)
                            .build(),
                    ptr
            );

            re.getBody().forEach(System.out::println);
            re.getBody().getLinks().forEach(System.out::println);
        };
    }

    @Bean
    CommandLineRunner fc(ReservationClient rc) {
        return args -> {
            rc.getReservations().forEach(System.out::println);
            System.out.println(rc.getReservation(1));
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class ReservationNamesRestController {

    @Autowired
    private ReservationClient client;

    @RequestMapping("/names")
    Collection<String> getNames() {
        return this.client.getReservations()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }
}

@FeignClient("reservation-service")
@RequestMapping(consumes = "application/hal+json")
interface ReservationClient {

    @RequestMapping(method = RequestMethod.GET, value = "/reservations")
    Resources<Reservation> getReservations();

    @RequestMapping(method = RequestMethod.GET, value = "/reservations/{id}")
    Resource<Reservation> getReservation(@PathVariable("id") long id);
}

class Reservation {
    private String reservationName;

    public String getReservationName() {
        return reservationName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("reservationName='").append(reservationName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
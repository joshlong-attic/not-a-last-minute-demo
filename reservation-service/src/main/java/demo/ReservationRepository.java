package demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Collection;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // select * from reservations where reservation_name = :rn
    @RestResource( path = "by-name")
    Collection<Reservation> findByReservationNameIgnoreCase(@Param("rn") String rn);
}

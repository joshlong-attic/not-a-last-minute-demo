package demo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class Reservation {

    @Id
    @GeneratedValue
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

    Reservation() { //why JPA why???
    }

    public Reservation(String reservationName) {

        this.reservationName = reservationName;
    }

    public Long getId() {

        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}

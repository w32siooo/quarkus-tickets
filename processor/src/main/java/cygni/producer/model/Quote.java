package cygni.producer.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

@RegisterForReflection
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Quote extends Hie {

    public String id;
    public int price;

    @Override
    public String toString() {
        return "Quote{" +
                "id='" + id + '\'' +
                ", price=" + price +
                '}';
    }
}

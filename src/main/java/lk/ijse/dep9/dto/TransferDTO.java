package lk.ijse.dep9.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDTO implements Serializable {
    private String type;
    private String from;
    private String to;
    private BigDecimal amount;

}

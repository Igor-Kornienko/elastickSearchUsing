import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class user {
    private String name;
    private String email;
    private String phoneNumber;
    private Date birth;
}

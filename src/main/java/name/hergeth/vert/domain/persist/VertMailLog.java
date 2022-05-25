package name.hergeth.vert.domain.persist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="VertMailLog")
public class VertMailLog {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "vid")
    private Long vid;

    @NotNull
    @Column(name = "SendDate", nullable = false)
    private LocalDateTime sendDate;

    @NotNull
    @Lob
    @Column(name = "Text", nullable = false)
    private String text;

    @Lob
    @Column(name = "Mail")
    private String mail;

    @Column(name = "dosend")
    private Boolean send;

    @Column(name = "time")
    private String time;

/*
    public VertMailLog(@NotNull boolean send, @NotNull VertVertretung text, String time, @NotNull String s, String dm) {
        this.text = text.toLog(s);
        this.sendDate = LocalDateTime.now();
        this.vid = text.getVno();
        this.mail = dm;
        this.send = send;
        this.time = time;
    }
*/

    public VertMailLog(@NotNull boolean send, String time, @NotNull String text, String mail) {
        this.text = text;
        this.sendDate = LocalDateTime.now();
        this.vid = -1L;
        this.mail = mail;
        this.send = send;
        this.time = time;
    }

    public VertMailLog(@NotNull boolean send, String time, @NotNull String text) {
        this.text = text;
        this.sendDate = LocalDateTime.now();
        this.vid = -1L;
        this.mail = "";
        this.send = send;
        this.time = time;
    }

    public final static String getCleanStr(){
        return "DELETE FROM MailLog WHERE dosend=false ";
    }
}

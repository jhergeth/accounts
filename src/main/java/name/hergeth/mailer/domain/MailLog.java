package name.hergeth.mailer.domain;

import de.bkgk.domain.ram.Vertretung;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name="MailLog")
public class MailLog{

    public MailLog(){}

    public MailLog(@NotNull boolean send, @NotNull Vertretung text, String time, @NotNull String s, String dm) {
        this.text = text.toLog(s);
        this.sendDate = LocalDateTime.now();
        this.vid = text.getVno();
        this.mail = dm;
        this.send = send;
        this.time = time;
    }

    public MailLog(@NotNull boolean send, String time, @NotNull String text, String dm) {
        this.text = text;
        this.sendDate = LocalDateTime.now();
        this.vid = -1L;
        this.mail = dm;
        this.send = send;
        this.time = time;
    }

    public MailLog(@NotNull boolean send, String time, @NotNull String text) {
        this.text = text;
        this.sendDate = LocalDateTime.now();
        this.vid = -1L;
        this.mail = "";
        this.send = send;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }

    public Long getVid() {
        return vid;
    }

    public void setVid(Long vid) {
        this.vid = vid;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Boolean getSend() {
        return send;
    }

    public void setSend(Boolean send) {
        this.send = send;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public final static String getCleanStr(){
        return "DELETE FROM MailLog WHERE dosend=false ";
    }

    @Override
    public String toString() {
        return "MailLog{" +
                "id=" + id +
                ", vid=" + vid +
                ", sendDate=" + sendDate +
                ", text='" + text + '\'' +
                ", mail='" + mail + '\'' +
                '}';
    }

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
}

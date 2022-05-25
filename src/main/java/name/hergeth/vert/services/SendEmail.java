package name.hergeth.vert.services;

import name.hergeth.vert.domain.ram.VertVertretung;

import java.util.List;

public interface SendEmail {
    void prepareMails(boolean send);

    void sendReminder(boolean send, String time, String kuk, List<VertVertretung> vList, List<VertVertretung> praesList, int vAnz, List<VertVertretung> vNeu);

    void sendReminderToBiGaKo(boolean send, String time, String kuk, List<VertVertretung> vList, List<VertVertretung> vNeu);

    void sendAnwesenheiten(VertVertretung v);

    String getComment(String kuk, String time, String template, Object vp);
}

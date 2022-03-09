package name.hergeth.mailer.service;

import de.bkgk.domain.ram.Vertretung;

import java.util.List;

public interface SendEmail {
    void prepareMails(boolean send);

    void sendReminder(boolean send, String time, String kuk, List<Vertretung> vList, List<Vertretung> praesList, int vAnz, List<Vertretung> vNeu);

    void sendReminderToBiGaKo(boolean send, String time, String kuk, List<Vertretung> vList, List<Vertretung> vNeu);

    void sendAnwesenheiten(Vertretung v);

    String getComment(String kuk, String time, String template, Object vp);
}

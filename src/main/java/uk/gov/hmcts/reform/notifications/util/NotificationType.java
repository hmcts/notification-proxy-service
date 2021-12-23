package uk.gov.hmcts.reform.notifications.util;

public enum NotificationType {
    EMAIL("Email"),
    LETTER("Letter");

    String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}

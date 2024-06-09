package net.pneumono.gravestones.gravestones;

@SuppressWarnings("unused")
public enum TimeFormatType {
    DDMMYYYY("dd/MM/yyyy HH:mm:ss"),
    MMDDYYYY("MM/dd/yyyy HH:mm:ss"),
    YYYYMMDD("yyyy/MM/dd HH:mm:ss");

    private final String format;

    TimeFormatType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}

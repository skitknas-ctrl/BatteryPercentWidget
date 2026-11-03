# Battery Percent Widget

En enkel hemskärmswidget för Android som visar batterinivån i procent.

- Uppdateras vid laddning/urkoppling, låg batterinivå och omstart
- Ingen konfiguration, inga behörigheter utöver `RECEIVE_BOOT_COMPLETED`
- Kräver Android 12 (API 31) eller senare

## Bygga

```
./gradlew assembleRelease
```

Widgeten läggs till via hemskärmens widgetmeny.

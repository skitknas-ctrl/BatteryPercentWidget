# Battery Percent Widget

En enkel hemskärmswidget för Android som visar batterinivån i procent.

- Uppdateras vid laddning/urkoppling, låg batterinivå och omstart
- Inställningar för mätarens ljushet och opacitet, textens färg och opacitet samt storlek
- Inga behörigheter utöver `RECEIVE_BOOT_COMPLETED`
- Kräver Android 12 (API 31) eller senare

## Bygga

```
./gradlew assembleRelease
```

Widgeten läggs till via hemskärmens widgetmeny. Inställningarna nås genom att öppna appen eller genom att hålla fingret på widgeten och välja ändra.

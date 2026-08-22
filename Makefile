.PHONY: libbox-android

# Воспроизводимая сборка нативного libbox.aar для Android ARM64.
libbox-android:
	bash scripts/build-libbox-android.sh

# Qibla Compass — Wear OS dev commands.
# Run from this directory, e.g. `make deploy`.

PKG := com.qibla.wear
ACT := $(PKG)/.MainActivity

.PHONY: help devices connect install run deploy restart logcat screenshot uninstall test clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS=":.*?## "}; {printf "  make %-12s %s\n", $$1, $$2}'

devices: ## List connected adb devices
	adb devices -l

connect: ## Connect to the watch over Wi-Fi: make connect IP=192.168.1.178:40597
	adb connect $(IP)

install: ## Build the debug APK and install it on the watch
	./gradlew :app:installDebug

run: ## Launch the app on the watch
	adb shell am start -n $(ACT)

deploy: install run ## Build, install, then launch (the usual one)

restart: ## Force-stop and relaunch (no rebuild)
	-adb shell am force-stop $(PKG)
	adb shell am start -n $(ACT)

logcat: ## Tail the app's logs (Ctrl-C to stop)
	adb logcat --pid=$$(adb shell pidof -s $(PKG))

screenshot: ## Save a screenshot to watch.png
	adb exec-out screencap -p > watch.png && echo "saved watch.png"

uninstall: ## Remove the app from the watch
	adb uninstall $(PKG)

test: ## Run the JVM unit tests (no device needed)
	./gradlew :jvm:test

clean: ## Clean Gradle build outputs
	./gradlew clean

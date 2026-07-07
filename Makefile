.PHONY: help build android desktop ios worker-dev worker-test dmg clean

help:
	@echo "Available commands:"
	@echo "  make build       Build the full project"
	@echo "  make android     Build the Android debug app"
	@echo "  make desktop     Build the desktop JVM artifact"
	@echo "  make ios         Check the iOS simulator framework link"
	@echo "  make worker-dev  Run the share worker in dev mode"
	@echo "  make worker-test Run share worker tests"
	@echo "  make dmg         Build the macOS DMG package"
	@echo "  make clean       Remove build outputs"

build:
	./gradlew build

android:
	./gradlew :composeApp:assembleDebug

desktop:
	./gradlew :composeApp:jvmJar

ios:
	./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

worker-dev:
	pnpm --dir shareWorker dev

worker-test:
	pnpm --dir shareWorker test

dmg:
	./gradlew :composeApp:packageDmg

clean:
	./gradlew clean

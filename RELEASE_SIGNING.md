# Yayın anahtarı (yalnızca senin için — depoya girmez)

Bu anahtar, GitHub'da yayınlanan APK'ların **senden** geldiğini kanıtlar.
Kaybedersen kullanıcılara güncelleme gönderemezsin. Şifreli bir yere yedekle
(örn. şifre yöneticisi + harici disk). Kimseyle paylaşma.

## 1. Anahtarı bir kez oluştur
Android Studio'nun içindeki keytool ile, proje klasörünün DIŞINDA:

    "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" \
      -genkeypair -v -keystore ~/Keys/wakelaunch-release.jks \
      -alias wakelaunch -keyalg RSA -keysize 4096 -validity 10000

Şifreyi soracak; güçlü bir şifre seç ve şifre yöneticine kaydet.

## 2. Proje köküne keystore.properties oluştur (.gitignore'da)

    storeFile=/Users/<kullanıcı>/Keys/wakelaunch-release.jks
    storePassword=<şifre>
    keyAlias=wakelaunch
    keyPassword=<şifre>

## 3. İmzalı yayın APK'sı
Android Studio → Build → Build APK(s) yerine terminalde:

    ./gradlew assembleRelease

Çıktı: app/build/outputs/apk/release/app-release.apk
→ WakeLaunch-1.0.0.apk olarak yeniden adlandır ve GitHub Releases'a yükle.

-obfuscationdictionary ./patterns.txt
-libraryjars /Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre/lib/rt.jar
-libraryjars ../libs/guava-15.0.jar
-injars GloBoxPerf-input.jar
-outjar GloBoxPerf.jar
-dontpreverify
-dontoptimize
-allowaccessmodification
-dontskipnonpubliclibraryclasses
-keepattributes Exceptions
-keepclassmembers,allowobfuscation,allowoptimization interface * extends java.rmi.Remote{*;}
-keep interface * extends java.rmi.Remote {
   <methods>;
}
-keep class * implements * {
    <init>(java.rmi.activation.ActivationID, java.rmi.MarshalledObject);
}
-keep public class com.google.common.base.**
-keep public class * {
    public static void main(java.lang.String[]);
}

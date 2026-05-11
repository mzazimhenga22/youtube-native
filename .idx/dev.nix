{ pkgs, lib, ... }: {

  # Add the packages that your project needs
  packages = [
    pkgs.jdk21
    pkgs.android-tools 
    pkgs.gradle
  ];

  # Set environment variables visible to your entire workspace
  env = {
    JAVA_HOME = "${pkgs.jdk21}/lib/openjdk";
    # The SDK is located here in this environment
    ANDROID_HOME = "/home/user/.androidsdkroot";
    ANDROID_SDK_ROOT = lib.mkForce "/home/user/.androidsdkroot";
  };

  # Add VS Code extensions
  idx = {
    extensions = [
      "vscjava.vscode-java-pack"
      "fwcd.kotlin"
      "vscjava.vscode-gradle"
    ];
    
    previews = {
      enable = true;
      previews = {
        android = {
          # Correct command and working directory
          command = ["./gradlew" "installDebug" "--no-daemon" "--parallel"];
          manager = "android";
          cwd = "android-native";
        };
      };
    };
  };
}

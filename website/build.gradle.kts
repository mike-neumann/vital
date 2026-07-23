import io.spring.gradle.dependencymanagement.org.codehaus.plexus.interpolation.os.Os

val pnpmInstallTask =
  tasks.register("pnpmInstall", Exec::class) {
    description = "This task will run 'pnpm install' to install all needed dependencies for the website."
    workingDir = projectDir
    inputs.files(layout.projectDirectory.dir("node_modules"))

    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
      commandLine("cmd", "/c", "pnpm", "install")
    } else {
      commandLine("pnpm", "install")
    }
  }

val npmBuildTask =
  tasks.register("pnpmBuild", Exec::class) {
    description = "This task will run 'pnpm run build' to build the static sources for the website."
    dependsOn(pnpmInstallTask)
    workingDir = projectDir
    inputs.files(layout.projectDirectory.dir("dist"))

    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
      commandLine("cmd", "/c", "pnpm", "run", "build")
    } else {
      commandLine("pnpm", "run", "build")
    }
  }

tasks.build {
  dependsOn(npmBuildTask)
}

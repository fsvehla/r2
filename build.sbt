/* for all projects builds */
ThisBuild / turbo := true

lazy val r2 = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name    := "r2",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= List(
      "dev.zio"  %% "zio"                         % Versions.zio,
      "io.r2dbc" %  "r2dbc-postgresql"            % "0.8.2.RELEASE",
      "dev.zio"  %% "zio-interop-reactivestreams" % "1.0.3.5-RC6",
      "dev.zio"  %% "zio-streams"                 % Versions.zio,
      "dev.zio"  %% "zio-test"                    % Versions.zio,
      "dev.zio"  %% "zio-test-sbt"                % Versions.zio
    ),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )

lazy val commonSettings = Vector(
  scalaVersion := "2.13.1",
  scalacOptions ++= Vector(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-language:higherKinds",
    "-language:existentials",
    "-Ywarn-unused:params,-implicits",
    "-Yrangepos",
    "-Xlint:_,-missing-interpolator,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )
)

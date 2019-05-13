import sbt.Keys.libraryDependencies

lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.5.22"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.arunma",
      scalaVersion    := "2.11.8"
    )),
    name := "RangerManagedHTTPService",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "org.apache.ranger" % "ranger-plugins-common" % "1.2.0",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test

    )
  )

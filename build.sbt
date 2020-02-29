lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaOrganization := "org.typelevel",
  scalaVersion := "2.12.4-bin-typelevel-4",
  scalacOptions ++= Seq(
    "-Yliteral-types"
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, http4s021, example)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "saul-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.0.0",
      "com.chuusai"   %% "shapeless" % "2.3.3"
    )
  )

lazy val http4s021 = project
  .in(file("http4s-0-21"))
  .settings(commonSettings)
  .settings(
    name := "saul-http4s-0-21",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.21.0"
    )
  )
  .dependsOn(core)

lazy val example = project
  .in(file("example"))
  .settings(commonSettings)
  .settings(
    name := "saul-example"
  )
  .dependsOn(core)

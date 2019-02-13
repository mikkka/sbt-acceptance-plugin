package name.mtkachev

import java.util.jar.JarFile

import sbt._
import Keys._
import sbt.testing.{Framework, SuiteSelector}

object RunAcceptance extends AutoPlugin {
  object autoImport {
    val acceptanceTestsPath = settingKey[String]("Acceptance tests path prefix")
    val acceptanceTests = taskKey[Seq[TestDefinition]]("Find acceptance tests")
    val runAcceptance = taskKey[Unit]("Run acceptance tests")

    val AcceptanceTest = config("acceptance").extend(Test)

    val locateAcceptanceTestsTask = Def.task {
      LocateAcceptanceTests(
        (Compile / unmanagedJars).value,
        (Test / loadedTestFrameworks).value,
        acceptanceTestsPath.value
      )
    }

    def acceptanceTestSettings: Seq[Def.Setting[_]] =
      Seq(
        libraryDependencies ++= Seq(
          "org.scalameta" %% "scalameta" % "4.1.0",
          "org.scalacheck" %% "scalacheck" % "1.14.0"
        )
      ) ++
      inConfig(AcceptanceTest)(
        Defaults.testSettings ++
        Seq(
          acceptanceTestsPath := "name/mtkachev",
          definedTests := locateAcceptanceTestsTask.value
        )
      )
  }

  import autoImport._

  override def requires = sbt.plugins.SbtPlugin
  override def projectConfigurations: Seq[Configuration] = Seq(AcceptanceTest)
  override def projectSettings: Seq[Def.Setting[_]] =
    acceptanceTestSettings
}

object LocateAcceptanceTests {
  import scala.collection.JavaConverters._
  def apply(
             unmanaged: Classpath,
             frameworks: Map[TestFramework, Framework],
             prefix: String
           ): Seq[TestDefinition] = {
    val testEntries = for {
      attr <- unmanaged
      file = attr.data
      jar = new JarFile(file.getAbsolutePath)
      jarEntry <- jar.entries().asScala
      if jarEntry.getName.endsWith("Spec.class") && jarEntry.getName.startsWith(prefix)
    } yield {
      jarEntry
    }

    val fingerprint = frameworks.values.flatMap(TestFramework.getFingerprints).head

    testEntries.map {x =>
      new TestDefinition(
        name = x.getName.replace('/', '.').dropRight(6),
        fingerprint = fingerprint,
        explicitlySpecified = false,
        selectors = Array(new SuiteSelector)
      )
    }
  }
}
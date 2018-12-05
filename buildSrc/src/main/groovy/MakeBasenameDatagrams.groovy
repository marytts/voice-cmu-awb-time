import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class MakeBasenameDatagrams extends DefaultTask {

    @Input
    Property<Integer> sampleRate = project.objects.property(Integer)

    @InputFile
    final RegularFileProperty basenamesFile = newInputFile()

    @InputDirectory
    final DirectoryProperty pmDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @TaskAction
    void make() {
        basenamesFile.get().asFile.eachLine('UTF-8') { basename ->
            def pmFile = pmDir.file("${basename}.pm").get().asFile
            def destFile = destDir.file("${basename}.json").get().asFile
            def lastTime = pmFile.readLines().last().split().first() as float
            def duration = (lastTime * sampleRate.get()) as long
            def json = new JsonBuilder([
                    [
                            duration: duration,
                            data    : basename.bytes.encodeBase64().toString()
                    ]
            ])
            destFile.text = json.toPrettyString()
        }
    }
}

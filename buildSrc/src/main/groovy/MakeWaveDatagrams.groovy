import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

class MakeWaveDatagrams extends DefaultTask {

    final WorkerExecutor workerExecutor

    @Input
    Property<Integer> sampleRate = project.objects.property(Integer)

    @InputFile
    final RegularFileProperty basenamesFile = newInputFile()

    @InputDirectory
    final DirectoryProperty wavDir = newInputDirectory()

    @InputDirectory
    final DirectoryProperty pmDir = newInputDirectory()

    @OutputDirectory
    final DirectoryProperty destDir = newOutputDirectory()

    @Inject
    MakeWaveDatagrams(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @TaskAction
    void make() {
        basenamesFile.get().asFile.eachLine('UTF-8') { basename ->
            def wavFile = wavDir.file("${basename}.wav").get().asFile
            def pmFile = pmDir.file("${basename}.pm").get().asFile
            def destFile = destDir.file("${basename}.json").get().asFile
            workerExecutor.submit(WaveDatagramMaker.class) { config ->
                config.params wavFile, pmFile, destFile, sampleRate.get()
            }
        }
    }
}

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.Task
import org.gradle.api.logging.Logger

class BuildTimeAnalyzerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val logger: Logger = project.logger
        val taskTimes = mutableMapOf<String, Long>()

        project.gradle.addListener(object : TaskExecutionListener {
            private var startTime: Long = 0

            override fun beforeExecute(task: Task) {
                startTime = System.nanoTime()
            }

            override fun afterExecute(task: Task, state: TaskState) {
                val timeTaken = (System.nanoTime() - startTime) / 1_000_000 // Convert to ms
                taskTimes[task.path] = timeTaken
            }
        })

        project.gradle.buildFinished {
            logger.lifecycle("🚀 Build Time Performance Report 🚀")
            val sortedTasks = taskTimes.entries.sortedByDescending { it.value }
            sortedTasks.forEach { (task, time) ->
                logger.lifecycle("Task: $task → ${time}ms")
            }
            val slowestTask = sortedTasks.firstOrNull()
            if (slowestTask != null) {
                logger.lifecycle("⏳ Slowest Task: ${slowestTask.key} took ${slowestTask.value}ms")
            }
        }
    }
}

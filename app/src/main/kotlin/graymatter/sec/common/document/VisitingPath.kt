package graymatter.sec.common.document

/**
 * Visiting some thing on path
 */
interface VisitingPath<T> {

    /**
     * Stop visiting asap
     */
    fun stop()

    /**
     * Item being visited
     */
    val subject: T

    /**
     * The path being visited.
     */
    val path: String


}

package co.makerflow.client.models


/**
 * Reason for a break during the workday.
 *
 * Can be one of the following:
 *             - coffee
 *             - tea
 *             - beverage
 *             - walk
 *             - lunch
 *             - running
 *             - workout
 *             - doctor
 *             - child
 *             - other
 */
enum class BreakReason {
    COFFEE, TEA, BEVERAGE, WALK, LUNCH, RUNNING, WORKOUT, DOCTOR, CHILD, OTHER;
}

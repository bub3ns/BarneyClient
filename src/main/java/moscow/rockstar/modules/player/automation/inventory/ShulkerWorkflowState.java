package moscow.rockstar.modules.player.automation.inventory;

enum ShulkerWorkflowState {
    IDLE,
    OPEN_SHULKER_CONTAINER,
    WAIT_FOR_CONTENT_SCREEN,
    TRANSFER_VALUABLE_ITEMS,
    CLOSE_SHULKER_CONTAINER,
    WAIT_FOR_CONTAINER_CLOSE,
    RETURN_SHULKER
}

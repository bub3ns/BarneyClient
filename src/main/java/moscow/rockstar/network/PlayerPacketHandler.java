package moscow.rockstar.network;

import com.mojang.authlib.GameProfile;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.InventoryState;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.bot.BotWorldState;
import moscow.rockstar.network.bot.BotSessionManager;
import moscow.rockstar.network.session.BotConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MoveMinecartAlongTrackS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProjectilePowerS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookSettingsS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TickStepS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateTickRateS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

/** Receives server state for one remote bot using the named Yarn packet API. */
public final class PlayerPacketHandler implements ClientPlayPacketListener, TickablePacketListener {
    private final BotConnection botConnection;
    private final BotController botController;
    private final InventoryState inventoryState;
    private final GameProfile gameProfile;

    public PlayerPacketHandler(BotConnection botConnection, BotController botController, GameProfile gameProfile) {
        this.botConnection = botConnection;
        this.botController = botController;
        this.inventoryState = botController.getInventoryState();
        this.gameProfile = gameProfile;
    }

    @Override
    public void tick() {
    }

    @Override
    public void onGameJoin(GameJoinS2CPacket packet) {
        this.accept(packet);
        this.inventoryState.setPlayerEntityId(packet.playerEntityId());
        this.botController.getWorldState().setLocalEntityId(packet.playerEntityId());
        RockstarClient.LOGGER.info("Bot {} joined game with entity id {}", this.botController.getBotName(), packet.playerEntityId());
    }

    @Override
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
        var position = packet.change();
        this.inventoryState.setPosition(position.position().x, position.position().y, position.position().z);
        this.inventoryState.setRotation(position.yaw(), position.pitch());
        this.inventoryState.setOnGround(true);
        this.botController.setVerticalMovementOverride(true);
        this.botController.getWorldState().applyServerCorrection(this.inventoryState);
        this.botConnection.getConnection().send(new TeleportConfirmC2SPacket(packet.teleportId()));
    }

    @Override
    public void onHealthUpdate(HealthUpdateS2CPacket packet) {
        this.inventoryState.setHealth(packet.getHealth());
        this.inventoryState.setFoodLevel(packet.getFood());
        if (this.inventoryState.isHealthDepleted() && this.botController.getControlState().isTargetHeightKnown()) {
            this.botConnection.getConnection().send(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        }
    }

    @Override
    public void onKeepAlive(KeepAliveS2CPacket packet) {
        this.botConnection.getConnection().send(new KeepAliveC2SPacket(packet.getId()));
    }

    @Override
    public void onPing(CommonPingS2CPacket packet) {
        this.botConnection.getConnection().send(new CommonPongC2SPacket(packet.getParameter()));
    }

    @Override
    public void onDisconnect(DisconnectS2CPacket packet) {
        String reason = packet.reason().getString();
        this.botConnection.handleDisconnect("Disconnected: " + reason);
        RockstarClient.LOGGER.info("Bot {} disconnected: {}", this.botController.getBotName(), reason);
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet) {
        this.inventoryState.setSelectedHotbarSlot(packet.slot());
    }

    @Override
    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
        this.inventoryState.updateExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
        this.accept(packet);
        this.inventoryState.resetState();
        this.inventoryState.setHealth(20.0f);
    }

    @Override
    public void onDeathMessage(DeathMessageS2CPacket packet) {
        if (this.botController.getControlState().isTargetHeightKnown()) {
            this.botConnection.getConnection().send(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        }
    }

    @Override
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (packet.getSyncId() == 0) {
            this.inventoryState.updateInventorySlot(packet.getSlot(), packet.getStack());
        } else {
            this.inventoryState.updateContainerSlot(packet.getSyncId(), packet.getRevision(), packet.getSlot(), packet.getStack());
        }
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
        if (packet.getSyncId() == 0) {
            this.inventoryState.setPlayerInventory(packet.getContents());
        } else {
            this.inventoryState.updateContainerContents(packet.getSyncId(), packet.getRevision(), packet.getContents());
            this.inventoryState.setCursorStack(packet.getCursorStack());
        }
    }

    @Override
    public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
        this.inventoryState.openContainer(packet.getSyncId(), "Horse " + packet.getHorseId());
    }

    @Override
    public void onOpenScreen(OpenScreenS2CPacket packet) {
        this.inventoryState.openContainer(packet.getSyncId(), packet.getName().getString());
    }

    @Override
    public void onCloseScreen(CloseScreenS2CPacket packet) {
        this.inventoryState.closeContainerIfMatches(packet.getSyncId());
    }

    @Override
    public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
        this.inventoryState.updatePlayerAbilities(packet.isInvulnerable(), packet.isFlying(), packet.allowFlying(), packet.isCreativeMode(), packet.getFlySpeed(), packet.getWalkSpeed());
    }

    @Override
    public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
        if (packet.getEntityId() != this.inventoryState.getPlayerEntityId() && packet.getEntityId() != this.botController.getWorldState().getTrackedEntityId()) {
            return;
        }
        for (var equipment : packet.getEquipmentList()) {
            this.inventoryState.setEquipmentItem(equipment.getFirst(), equipment.getSecond());
        }
    }

    @Override
    public void onGameMessage(GameMessageS2CPacket packet) {
        BotSessionManager.getInstance().handleBalanceMessage(this.botController, packet.content().getString());
    }

    @Override
    public void onChatMessage(ChatMessageS2CPacket packet) {
        if (packet.unsignedContent() != null) {
            BotSessionManager.getInstance().handleBalanceMessage(this.botController, packet.unsignedContent().getString());
        }
    }

    @Override
    public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet) {
        BotSessionManager.getInstance().handleBalanceMessage(this.botController, packet.message().getString());
    }

    @Override
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
        this.inventoryState.setWorldTime((int)packet.timeOfDay());
    }

    @Override
    public void onSetCursorItem(SetCursorItemS2CPacket packet) {
        this.inventoryState.setCursorStack(packet.contents());
    }

    @Override
    public void onSetPlayerInventory(SetPlayerInventoryS2CPacket packet) {
        this.inventoryState.setInventoryItem(packet.slot(), packet.contents());
    }

    @Override
    public void onBlockUpdate(BlockUpdateS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onChunkData(ChunkDataS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntity(EntityS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntityPosition(EntityPositionS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntityPositionSync(EntityPositionSyncS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntityVelocityUpdate(net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onPlayerRemove(PlayerRemoveS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onPlayerList(PlayerListS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
        this.accept(packet);
    }

    @Override
    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
        this.accept(packet);
    }

    private void accept(Object packet) {
        this.botController.getWorldState().acceptPacket(packet);
    }

    @Override public void onEntitySpawn(EntitySpawnS2CPacket packet) { accept(packet); }
    @Override public void onExperienceOrbSpawn(ExperienceOrbSpawnS2CPacket packet) { accept(packet); }
    @Override public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) { accept(packet); }
    @Override public void onEntityAnimation(EntityAnimationS2CPacket packet) { accept(packet); }
    @Override public void onDamageTilt(DamageTiltS2CPacket packet) { accept(packet); }
    @Override public void onStatistics(net.minecraft.network.packet.s2c.play.StatisticsS2CPacket packet) { accept(packet); }
    @Override public void onRecipeBookAdd(RecipeBookAddS2CPacket packet) { accept(packet); }
    @Override public void onRecipeBookRemove(RecipeBookRemoveS2CPacket packet) { accept(packet); }
    @Override public void onRecipeBookSettings(RecipeBookSettingsS2CPacket packet) { accept(packet); }
    @Override public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket packet) { accept(packet); }
    @Override public void onSignEditorOpen(SignEditorOpenS2CPacket packet) { accept(packet); }
    @Override public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) { accept(packet); }
    @Override public void onBlockEvent(BlockEventS2CPacket packet) { accept(packet); }
    @Override public void onRemoveMessage(RemoveMessageS2CPacket packet) { accept(packet); }
    @Override public void onMapUpdate(MapUpdateS2CPacket packet) { accept(packet); }
    @Override public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) { accept(packet); }
    @Override public void onEntityStatus(EntityStatusS2CPacket packet) { accept(packet); }
    @Override public void onEntityAttach(EntityAttachS2CPacket packet) { accept(packet); }
    @Override public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) { accept(packet); }
    @Override public void onExplosion(ExplosionS2CPacket packet) { accept(packet); }
    @Override public void onGameStateChange(GameStateChangeS2CPacket packet) { accept(packet); }
    @Override public void onChunkBiomeData(ChunkBiomeDataS2CPacket packet) { accept(packet); }
    @Override public void onUnloadChunk(UnloadChunkS2CPacket packet) { accept(packet); }
    @Override public void onWorldEvent(WorldEventS2CPacket packet) { accept(packet); }
    @Override public void onMoveMinecartAlongTrack(MoveMinecartAlongTrackS2CPacket packet) { accept(packet); }
    @Override public void onPlayerRotation(PlayerRotationS2CPacket packet) { accept(packet); }
    @Override public void onParticle(ParticleS2CPacket packet) { accept(packet); }
    @Override public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet) { accept(packet); }
    @Override public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) { accept(packet); }
    @Override public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) { accept(packet); }
    @Override public void onTeam(TeamS2CPacket packet) { accept(packet); }
    @Override public void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket packet) { accept(packet); }
    @Override public void onScoreboardScoreReset(ScoreboardScoreResetS2CPacket packet) { accept(packet); }
    @Override public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) { accept(packet); }
    @Override public void onPlaySound(PlaySoundS2CPacket packet) { accept(packet); }
    @Override public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) { accept(packet); }
    @Override public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) { accept(packet); }
    @Override public void onUpdateTickRate(UpdateTickRateS2CPacket packet) { accept(packet); }
    @Override public void onTickStep(TickStepS2CPacket packet) { accept(packet); }
    @Override public void onEntityAttributes(EntityAttributesS2CPacket packet) { accept(packet); }
    @Override public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet) { accept(packet); }
    @Override public void onEndCombat(EndCombatS2CPacket packet) { accept(packet); }
    @Override public void onEnterCombat(EnterCombatS2CPacket packet) { accept(packet); }
    @Override public void onDifficulty(DifficultyS2CPacket packet) { accept(packet); }
    @Override public void onSetCameraEntity(SetCameraEntityS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket packet) { accept(packet); }
    @Override public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket packet) { accept(packet); }
    @Override public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) { accept(packet); }
    @Override public void onBossBar(BossBarS2CPacket packet) { accept(packet); }
    @Override public void onCooldownUpdate(CooldownUpdateS2CPacket packet) { accept(packet); }
    @Override public void onVehicleMove(VehicleMoveS2CPacket packet) { accept(packet); }
    @Override public void onAdvancements(AdvancementUpdateS2CPacket packet) { accept(packet); }
    @Override public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) { accept(packet); }
    @Override public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) { accept(packet); }
    @Override public void onCommandTree(CommandTreeS2CPacket packet) { accept(packet); }
    @Override public void onStopSound(StopSoundS2CPacket packet) { accept(packet); }
    @Override public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) { accept(packet); }
    @Override public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) { accept(packet); }
    @Override public void onLookAt(LookAtS2CPacket packet) { accept(packet); }
    @Override public void onNbtQueryResponse(NbtQueryResponseS2CPacket packet) { accept(packet); }
    @Override public void onLightUpdate(LightUpdateS2CPacket packet) { accept(packet); }
    @Override public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) { accept(packet); }
    @Override public void onSetTradeOffers(SetTradeOffersS2CPacket packet) { accept(packet); }
    @Override public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) { accept(packet); }
    @Override public void onSimulationDistance(SimulationDistanceS2CPacket packet) { accept(packet); }
    @Override public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) { accept(packet); }
    @Override public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) { accept(packet); }
    @Override public void onOverlayMessage(OverlayMessageS2CPacket packet) { accept(packet); }
    @Override public void onSubtitle(SubtitleS2CPacket packet) { accept(packet); }
    @Override public void onTitle(TitleS2CPacket packet) { accept(packet); }
    @Override public void onTitleFade(TitleFadeS2CPacket packet) { accept(packet); }
    @Override public void onTitleClear(ClearTitleS2CPacket packet) { accept(packet); }
    @Override public void onServerMetadata(ServerMetadataS2CPacket packet) { accept(packet); }
    @Override public void onChatSuggestions(ChatSuggestionsS2CPacket packet) { accept(packet); }
    @Override public void onBundle(BundleS2CPacket packet) { accept(packet); }
    @Override public void onEntityDamage(EntityDamageS2CPacket packet) { accept(packet); }
    @Override public void onEnterReconfiguration(EnterReconfigurationS2CPacket packet) { accept(packet); }
    @Override public void onStartChunkSend(StartChunkSendS2CPacket packet) { accept(packet); }
    @Override public void onChunkSent(ChunkSentS2CPacket packet) { accept(packet); }
    @Override public void onDebugSample(DebugSampleS2CPacket packet) { accept(packet); }
    @Override public void onProjectilePower(ProjectilePowerS2CPacket packet) { accept(packet); }

    @Override public void onCustomPayload(CustomPayloadS2CPacket packet) { }
    @Override public void onResourcePackSend(ResourcePackSendS2CPacket packet) { }
    @Override public void onResourcePackRemove(ResourcePackRemoveS2CPacket packet) { }
    @Override public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) { }
    @Override public void onStoreCookie(StoreCookieS2CPacket packet) { }
    @Override public void onServerTransfer(ServerTransferS2CPacket packet) { }
    @Override public void onCustomReportDetails(CustomReportDetailsS2CPacket packet) { }
    @Override public void onServerLinks(ServerLinksS2CPacket packet) { }
    @Override public void onCookieRequest(CookieRequestS2CPacket packet) { }
    @Override public void onPingResult(PingResultS2CPacket packet) { }

    @Override public boolean isConnectionOpen() {
        return this.botConnection.getConnection() != null && this.botConnection.getConnection().isOpen();
    }

    @Override public NetworkSide getSide() {
        return NetworkSide.CLIENTBOUND;
    }

    @Override public NetworkPhase getPhase() {
        return NetworkPhase.PLAY;
    }

    @Override public void onDisconnected(DisconnectionInfo info) {
        this.botConnection.handleDisconnect(info.reason().getString());
        RockstarClient.LOGGER.info("Bot {} connection closed", this.botController.getBotName());
    }

    public BotConnection getBotConnection() {
        return this.botConnection;
    }

    public BotController getBotController() {
        return this.botController;
    }

    public InventoryState getInventoryState() {
        return this.inventoryState;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}

package moscow.rockstar.render.texture;

import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

public final class TextureOverrideState {
    final Object owner;
    volatile Identifier primaryTextureId;
    volatile SkinTextures.Model modelVariant;
    volatile Identifier secondaryTextureId;
    volatile Identifier tertiaryTextureId;
    volatile boolean useDefaultModel;

    TextureOverrideState(Object owner) {
        this.owner = owner;
    }

    public void setPrimaryTexture(Identifier textureId) {
        this.primaryTextureId = textureId;
    }

    public void setModelVariant(SkinTextures.Model modelVariant) {
        this.modelVariant = modelVariant;
    }

    public void setSecondaryTexture(Identifier textureId) {
        this.secondaryTextureId = textureId;
        if (textureId != null) {
            this.useDefaultModel = false;
        }
    }

    public void setTertiaryTexture(Identifier textureId) {
        this.tertiaryTextureId = textureId;
    }

    public void setUseDefaultModel(boolean useDefaultModel) {
        this.useDefaultModel = useDefaultModel;
        if (useDefaultModel) {
            this.secondaryTextureId = null;
        }
    }

    public Identifier getPrimaryTexture() {
        return this.primaryTextureId;
    }

    public Identifier getSecondaryTexture() {
        return this.secondaryTextureId;
    }

    public Identifier getTertiaryTexture() {
        return this.tertiaryTextureId;
    }

    public SkinTextures.Model getModelVariant() {
        return this.modelVariant;
    }

    public boolean usesDefaultModel() {
        return this.useDefaultModel;
    }

    public boolean isEmpty() {
        return this.primaryTextureId == null && this.modelVariant == null
                && this.secondaryTextureId == null && this.tertiaryTextureId == null
                && !this.useDefaultModel;
    }
}

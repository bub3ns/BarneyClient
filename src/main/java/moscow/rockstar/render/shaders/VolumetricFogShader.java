package moscow.rockstar.render.shaders;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/** The {@code volumetric_fog} shader program and the uniform block the fog pass fills in. */
public final class VolumetricFogShader extends ShaderProgramBase {
    private GlUniform inverseViewProjection;
    private GlUniform cameraPosition;
    private GlUniform fogTint;
    private GlUniform flow;
    private GlUniform flowPatch;
    private GlUniform density;
    private GlUniform layerY;
    private GlUniform thickness;
    private GlUniform maxDistance;
    private GlUniform coverage;
    private GlUniform tileScale;
    private GlUniform stepWorld;

    public VolumetricFogShader(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.inverseViewProjection = this.getUniform("InvViewProj");
        this.cameraPosition = this.getUniform("CamPos");
        this.fogTint = this.getUniform("FogTint");
        this.flow = this.getUniform("Flow");
        this.flowPatch = this.getUniform("FlowPatch");
        this.density = this.getUniform("Density");
        this.layerY = this.getUniform("LayerY");
        this.thickness = this.getUniform("Thickness");
        this.maxDistance = this.getUniform("MaxDistance");
        this.coverage = this.getUniform("Coverage");
        this.tileScale = this.getUniform("TileScale");
        this.stepWorld = this.getUniform("StepWorld");
        super.initializeShaderUniforms();
    }

    public void uploadUniforms(Matrix4f matrix4f, FogUniforms uniforms) {
        if (this.inverseViewProjection != null) this.inverseViewProjection.set(matrix4f);
        if (this.cameraPosition != null) this.cameraPosition.set(uniforms.cameraX, uniforms.cameraY, uniforms.cameraZ);
        if (this.fogTint != null) this.fogTint.set(uniforms.tintRed, uniforms.tintGreen, uniforms.tintBlue);
        if (this.flow != null) this.flow.set(uniforms.flowX, uniforms.flowZ);
        if (this.flowPatch != null) this.flowPatch.set(uniforms.patchX, uniforms.patchZ);
        if (this.density != null) this.density.set(uniforms.density);
        if (this.layerY != null) this.layerY.set(uniforms.layerY);
        if (this.thickness != null) this.thickness.set(uniforms.thickness);
        if (this.maxDistance != null) this.maxDistance.set(uniforms.maxDistance);
        if (this.coverage != null) this.coverage.set(uniforms.coverage);
        if (this.tileScale != null) this.tileScale.set(uniforms.tileScale);
        if (this.stepWorld != null) this.stepWorld.set(uniforms.stepWorld);
    }

    public static final class FogUniforms {
        public float cameraX;
        public float cameraY;
        public float cameraZ;
        public float tintRed = 1.0f;
        public float tintGreen = 1.0f;
        public float tintBlue = 1.0f;
        public float flowX;
        public float flowZ;
        public float patchX;
        public float patchZ;
        public float density = 0.35f;
        public float layerY = 64.0f;
        public float thickness = 10.0f;
        public float maxDistance = 96.0f;
        public float coverage = 0.45f;
        public float tileScale = 0.0034722222f;
        public float stepWorld = 4.5f;
        /** GL id of the procedural noise texture bound to Sampler1; 0 means "not ready, skip the pass". */
        public int noiseTexture;
        public boolean highQuality;

        public FogUniforms setCameraPosition(float x, float y, float z) { this.cameraX = x; this.cameraY = y; this.cameraZ = z; return this; }
        public FogUniforms setTint(float red, float green, float blue) { this.tintRed = red; this.tintGreen = green; this.tintBlue = blue; return this; }
        public FogUniforms setFlow(float x, float z) { this.flowX = x; this.flowZ = z; return this; }
        public FogUniforms setFlowPatch(float x, float z) { this.patchX = x; this.patchZ = z; return this; }
        public FogUniforms setDensity(float value) { this.density = value; return this; }
        public FogUniforms setLayer(float value) { this.layerY = value; return this; }
        public FogUniforms setThickness(float value) { this.thickness = value; return this; }
        public FogUniforms setMaxDistance(float value) { this.maxDistance = value; return this; }
        public FogUniforms setCoverage(float value) { this.coverage = value; return this; }
        public FogUniforms setTileScale(float value) { this.tileScale = value; return this; }
        public FogUniforms setStepWorld(float value) { this.stepWorld = value; return this; }
        public FogUniforms setHighQuality(boolean value) { this.highQuality = value; return this; }
    }
}

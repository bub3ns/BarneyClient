package moscow.rockstar.modules.visuals.effects.particles;

public record ParticleState(ParticleInstance particle, float x, float y, float z,
                            float intensity, double distance) {
    public double getRenderDistance() {
        return this.particle.closing ? this.distance * 0.5 : this.distance;
    }

    public float distanceTo(ParticleState other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public ParticleState withIntensityOffset(float offset) {
        return new ParticleState(this.particle, this.x, this.y, this.z,
                this.intensity + offset, this.distance);
    }

    public ParticleInstance getParticle() {
        return this.particle;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getIntensity() {
        return this.intensity;
    }

    public double getDistance() {
        return this.distance;
    }
}

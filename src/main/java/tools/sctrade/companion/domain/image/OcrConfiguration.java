package tools.sctrade.companion.domain.image;

public record OcrConfiguration(boolean convertToGreyscale, boolean invertColors,
    float brightnessFactor, int contrastOffset) {
  public boolean shouldRescale() {
    return (brightnessFactor != 0.0f && contrastOffset != 0);
  }
}

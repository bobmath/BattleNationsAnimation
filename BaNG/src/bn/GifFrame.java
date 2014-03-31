package bn;

import java.awt.image.BufferedImage;

public class GifFrame
{
   public static final String NONE                = "none";
   public static final String DO_NOT_DISPOSE      = "doNotDispose";
   public static final String RESTORE_TO_BGCOLOR  = "restoreToBackgroundColor";
   public static final String RESTORE_TO_PREVIOUS = "restoreToPrevious";

   public final BufferedImage img;
   public final long          delay; // in millis
   public final String        disposalMethod;

   public GifFrame(BufferedImage img, long delay)
   {
      this(img, delay, NONE);
   }

   public GifFrame(BufferedImage img, long delay, String disposalMethod)
   {
      this.img = img;
      this.delay = delay;
      this.disposalMethod = disposalMethod;
   }
}


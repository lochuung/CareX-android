package hcmute.edu.vn.loclinhvabao.carex.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import hcmute.edu.vn.loclinhvabao.carex.R;

/**
 * Utility class for image loading operations
 * Uses Glide for efficient image loading with caching and transformation
 */
public class ImageUtils {

    /**
     * Load an image with rounded corners and proper loading indicators
     *
     * @param context The context
     * @param imageUrl The image URL to load
     * @param imageView The target ImageView
     * @param cornerRadius Radius for rounded corners in dp
     * @param onLoadListener Optional listener for image loading events
     */
    public static void loadRoundedImage(
            Context context, 
            String imageUrl, 
            ImageView imageView, 
            int cornerRadius,
            @Nullable OnImageLoadListener onLoadListener) {
        
        // Create request options with placeholder, error image, and transformations
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transforms(new CenterCrop(), new RoundedCorners(cornerRadius));

        // Create the request
        RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade());

        // Add listener if provided
        if (onLoadListener != null) {
            requestBuilder.listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    onLoadListener.onLoadFailed();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    onLoadListener.onLoadSuccess();
                    return false;
                }
            });
        }

        // Execute the request
        requestBuilder.into(imageView);
    }

    /**
     * Simpler version without load listener
     */
    public static void loadRoundedImage(
            Context context,
            String imageUrl,
            ImageView imageView,
            int cornerRadius) {
        loadRoundedImage(context, imageUrl, imageView, cornerRadius, null);
    }

    /**
     * Interface for image loading callbacks
     */
    public interface OnImageLoadListener {
        void onLoadSuccess();
        void onLoadFailed();
    }
}

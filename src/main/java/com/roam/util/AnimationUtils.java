package com.roam.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Utility class for smooth animations throughout the application.
 * <p>
 * Provides consistent, modern animations for UI elements including:
 * </p>
 * <ul>
 * <li>Entrance animations (fade in, slide in, scale in, pop in)</li>
 * <li>Exit animations (fade out, slide out, scale out)</li>
 * <li>Stagger animations for list items</li>
 * <li>Attention animations (shake, pulse, bounce)</li>
 * <li>Transition helpers for smooth state changes</li>
 * </ul>
 * <p>
 * All animations use predefined duration constants and easing functions
 * to ensure visual consistency across the application.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class AnimationUtils {

    // ==================== Animation Duration Constants ====================

    /** Fast animation duration (150ms) - used for quick transitions. */
    public static final Duration FAST = Duration.millis(150);
    /** Normal animation duration (250ms) - default for most animations. */
    public static final Duration NORMAL = Duration.millis(250);
    /** Slow animation duration (400ms) - used for emphasis. */
    public static final Duration SLOW = Duration.millis(400);
    /** Very slow animation duration (600ms) - used for dramatic effects. */
    public static final Duration VERY_SLOW = Duration.millis(600);

    // ==================== Easing Function Constants ====================

    /** Ease-out interpolator - fast start, slow end. */
    public static final Interpolator EASE_OUT = Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0);
    /** Ease-in-out interpolator - slow start and end, fast middle. */
    public static final Interpolator EASE_IN_OUT = Interpolator.SPLINE(0.42, 0.0, 0.58, 1.0);
    /** Spring interpolator - natural bouncy feel. */
    public static final Interpolator SPRING = Interpolator.SPLINE(0.175, 0.885, 0.32, 1.0);
    /** Bounce interpolator - overshoots then settles. */
    public static final Interpolator BOUNCE = Interpolator.SPLINE(0.34, 1.0, 0.64, 1.0);

    // ==================== Constructor ====================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private AnimationUtils() {
        // Utility class
    }

    // ==================== Entrance Animations ====================

    /**
     * Fade in animation for a node.
     */
    public static void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(EASE_OUT);
        ft.play();
    }

    public static void fadeIn(Node node) {
        fadeIn(node, NORMAL);
    }

    /**
     * Slide in from bottom animation.
     */
    public static void slideInFromBottom(Node node, Duration duration, double distance) {
        node.setTranslateY(distance);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createTranslateY(node, distance, 0, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(EASE_OUT);
        pt.play();
    }

    public static void slideInFromBottom(Node node) {
        slideInFromBottom(node, NORMAL, 30);
    }

    /**
     * Slide in from top animation.
     */
    public static void slideInFromTop(Node node, Duration duration, double distance) {
        node.setTranslateY(-distance);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createTranslateY(node, -distance, 0, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(EASE_OUT);
        pt.play();
    }

    /**
     * Slide in from left animation.
     */
    public static void slideInFromLeft(Node node, Duration duration, double distance) {
        node.setTranslateX(-distance);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createTranslateX(node, -distance, 0, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(EASE_OUT);
        pt.play();
    }

    /**
     * Slide in from right animation.
     */
    public static void slideInFromRight(Node node, Duration duration, double distance) {
        node.setTranslateX(distance);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createTranslateX(node, distance, 0, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(EASE_OUT);
        pt.play();
    }

    /**
     * Scale in (zoom) animation - good for cards and modals.
     */
    public static void scaleIn(Node node, Duration duration) {
        node.setScaleX(0.8);
        node.setScaleY(0.8);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createScale(node, 0.8, 1, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(SPRING);
        pt.play();
    }

    public static void scaleIn(Node node) {
        scaleIn(node, NORMAL);
    }

    /**
     * Pop in animation with bounce effect.
     */
    public static void popIn(Node node, Duration duration) {
        node.setScaleX(0.5);
        node.setScaleY(0.5);
        node.setOpacity(0);

        ParallelTransition pt = new ParallelTransition(
                createScale(node, 0.5, 1, duration),
                createFade(node, 0, 1, duration));
        pt.setInterpolator(BOUNCE);
        pt.play();
    }

    public static void popIn(Node node) {
        popIn(node, NORMAL);
    }

    // ==================== Stagger Animations ====================

    /**
     * Animates children of a pane with staggered entrance.
     * Each child enters with a slight delay after the previous one.
     */
    public static void staggerFadeIn(Pane parent, Duration baseDelay, Duration stagger) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            child.setOpacity(0);

            FadeTransition ft = new FadeTransition(NORMAL, child);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(baseDelay.add(stagger.multiply(i)));
            ft.setInterpolator(EASE_OUT);
            ft.play();
        }
    }

    /**
     * Staggered slide-up animation for list items.
     */
    public static void staggerSlideUp(Pane parent, Duration baseDelay, Duration stagger) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            child.setOpacity(0);
            child.setTranslateY(20);

            ParallelTransition pt = new ParallelTransition(
                    createFade(child, 0, 1, NORMAL),
                    createTranslateY(child, 20, 0, NORMAL));
            pt.setDelay(baseDelay.add(stagger.multiply(i)));
            pt.setInterpolator(EASE_OUT);
            pt.play();
        }
    }

    /**
     * Staggered scale animation for grid items like stats cards.
     */
    public static void staggerScaleIn(Pane parent, Duration baseDelay, Duration stagger) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            child.setOpacity(0);
            child.setScaleX(0.8);
            child.setScaleY(0.8);

            ParallelTransition pt = new ParallelTransition(
                    createFade(child, 0, 1, NORMAL),
                    createScale(child, 0.8, 1, NORMAL));
            pt.setDelay(baseDelay.add(stagger.multiply(i)));
            pt.setInterpolator(SPRING);
            pt.play();
        }
    }

    // ==================== Interactive Animations ====================

    /**
     * Hover scale effect - enlarges slightly on hover.
     */
    public static void addHoverScale(Node node, double scaleFactor) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(scaleFactor);
            st.setToY(scaleFactor);
            st.setInterpolator(EASE_OUT);
            st.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(FAST, node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(EASE_OUT);
            st.play();
        });
    }

    /**
     * Click/press scale effect - shrinks slightly when pressed.
     */
    public static void addPressEffect(Node node) {
        node.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });

        node.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(BOUNCE);
            st.play();
        });
    }

    /**
     * Subtle lift effect on hover (scale + shadow simulation via translate).
     */
    public static void addLiftEffect(Node node) {
        node.setOnMouseEntered(e -> {
            ParallelTransition pt = new ParallelTransition(
                    createScale(node, node.getScaleX(), 1.02, FAST),
                    createTranslateY(node, node.getTranslateY(), -2, FAST));
            pt.setInterpolator(EASE_OUT);
            pt.play();
        });

        node.setOnMouseExited(e -> {
            ParallelTransition pt = new ParallelTransition(
                    createScale(node, node.getScaleX(), 1.0, FAST),
                    createTranslateY(node, node.getTranslateY(), 0, FAST));
            pt.setInterpolator(EASE_OUT);
            pt.play();
        });
    }

    // ==================== Continuous Animations ====================

    /**
     * Gentle pulse animation - good for drawing attention.
     */
    public static void addPulse(Node node, Duration duration, double scale) {
        ScaleTransition st = new ScaleTransition(duration, node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(scale);
        st.setToY(scale);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.setInterpolator(EASE_IN_OUT);
        st.play();
    }

    /**
     * Gentle floating animation.
     */
    public static void addFloat(Node node, Duration duration, double distance) {
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromY(0);
        tt.setToY(-distance);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(EASE_IN_OUT);
        tt.play();
    }

    /**
     * Shimmer/glow effect via opacity.
     */
    public static void addShimmer(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0.7);
        ft.setToValue(1.0);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.setInterpolator(EASE_IN_OUT);
        ft.play();
    }

    // ==================== Exit Animations ====================

    /**
     * Fade out animation.
     */
    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(EASE_OUT);
        if (onFinished != null) {
            ft.setOnFinished(e -> onFinished.run());
        }
        ft.play();
    }

    /**
     * Scale out animation.
     */
    public static void scaleOut(Node node, Duration duration, Runnable onFinished) {
        ParallelTransition pt = new ParallelTransition(
                createScale(node, 1, 0.8, duration),
                createFade(node, 1, 0, duration));
        pt.setInterpolator(EASE_OUT);
        if (onFinished != null) {
            pt.setOnFinished(e -> onFinished.run());
        }
        pt.play();
    }

    /**
     * View transition - fades out old node and fades in new node.
     */
    public static void transitionViews(Pane container, Node oldView, Node newView, Duration duration) {
        newView.setOpacity(0);
        container.getChildren().add(newView);

        FadeTransition fadeOut = new FadeTransition(duration.divide(2), oldView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(duration.divide(2), newView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(EASE_OUT);

        fadeOut.setOnFinished(e -> {
            container.getChildren().remove(oldView);
            fadeIn.play();
        });

        fadeOut.play();
    }

    // ==================== Helper Methods ====================

    private static TranslateTransition createTranslateY(Node node, double from, double to, Duration duration) {
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromY(from);
        tt.setToY(to);
        return tt;
    }

    private static TranslateTransition createTranslateX(Node node, double from, double to, Duration duration) {
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setFromX(from);
        tt.setToX(to);
        return tt;
    }

    private static FadeTransition createFade(Node node, double from, double to, Duration duration) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }

    private static ScaleTransition createScale(Node node, double from, double to, Duration duration) {
        ScaleTransition st = new ScaleTransition(duration, node);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        return st;
    }
}

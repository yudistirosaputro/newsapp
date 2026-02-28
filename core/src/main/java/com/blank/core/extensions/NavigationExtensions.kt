package com.blank.core.extensions

import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions

/**
 * Navigate to a destination using deep link URI.
 * Use for simple navigation without arguments.
 *
 * @param uri The deep link URI (e.g., "app://newsapp/home")
 * @param navOptions Optional navigation options
 */
fun NavController.navigateDeepLink(
    uri: String,
    navOptions: NavOptions? = null,
) {
    val request = NavDeepLinkRequest.Builder
        .fromUri(Uri.parse(uri))
        .build()
    navigate(request, navOptions)
}

/**
 * Navigate to a destination using resource ID with arguments.
 * Use when passing Parcelable or complex arguments.
 *
 * @param destinationId The destination resource ID (e.g., R.id.detailArticleFragment)
 * @param args The arguments bundle to pass to the destination
 * @param navOptions Optional navigation options
 */
fun NavController.navigateWithArgs(
    @IdRes destinationId: Int,
    args: Bundle,
    navOptions: NavOptions? = null,
) {
    navigate(destinationId, args, navOptions)
}

/**
 * Navigate to a destination using resource ID with a single Parcelable argument.
 * Use when passing a single Parcelable object.
 *
 * @param destinationId The destination resource ID (e.g., R.id.detailArticleFragment)
 * @param key The argument key (must match the argument name in nav_graph.xml)
 * @param value The Parcelable value to pass
 * @param navOptions Optional navigation options
 */
fun <T : android.os.Parcelable> NavController.navigateWithParcelable(
    @IdRes destinationId: Int,
    key: String,
    value: T,
    navOptions: NavOptions? = null,
) {
    val args = Bundle().apply {
        putParcelable(key, value)
    }
    navigate(destinationId, args, navOptions)
}
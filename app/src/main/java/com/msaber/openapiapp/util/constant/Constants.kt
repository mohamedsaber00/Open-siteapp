package com.msaber.openapiapp.util.constant

 class Constants {
     companion object{
         val PASSWORD_RESER_URL = "https://open-api.xyz/password_reset/"
         const val BASE_URL = "https://open-api.xyz/api/"

         const val NETWORK_TIMEOUT = 6000L

         const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
         const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing
         const val PAGINATION_PAGE_SIZE = 10
         const val GALLERY_REQUEST_CODE = 201
         const val PERMISSION_REQUEST_READ_STORAGE: Int = 301
         const val CROP_IMAGE_INTENT_CODE = 401
     }
 }
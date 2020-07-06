# Open Site

A demo android application to fully interact with https://open-api.xyz.

## Screenshots:

<img src="https://i.imgur.com/J6aYnSZ.jpeg" alt="drawing" width="200"/> <img src="https://i.imgur.com/SCnhcFC.jpeg" alt="drawing" width="200"/> <img src="https://i.imgur.com/EZSc0m9.jpg" alt="drawing" width="200"/>

## Features:
##### Authntication :
- Sign in with current account  
- Register new account 
 - Reset password

##### Blogs :
- View all blogs 
- Filtering blogs by date and author
- Search blogs 
- Create new blog (With a photo)
- Delete one of your blogs
- Update a blog
##### Account :
- View account information 
- Update email and username
- Change password


# Tech stack

-  Written in [**Kotlin**](https://kotlinlang.org/) 
- [**Coroutines**](https://github.com/Kotlin/kotlinx.coroutines) : Lightweight threads for asynchronous.
-   Dependency injection using **Dagger 2**
-   JetPack
    -   **LiveData** - Observe data and apply it to views.
    -   **Lifecycle** - dispose of observing data when lifecycle state changes.
    -   **ViewMode**l - UI related data holder, lifecycle aware.
    -   **Room Persistence** - build a database using the abstract layer.
    - **Navigation** : Control navigation between fragment 
         - Leveraging multiple navigation graphs
- **Architecture : Model View Intent (MVI)** :
     - StateEvent: used to trigger possible events  
     - DataState: Handle response of data (loading, error, data) and emits it ViewState
     - ViewState: contains all values of view fields 
- **[Retrofit2]**(https://github.com/square/retrofit) - build the REST APIs and get network data
- **Gson** : Handle json objects
-**Pagination, Search, fitler** : Control objects returned from server and database cache
- **Single source of truth** 
## MVI Architecture 
<img src="https://miro.medium.com/max/1400/1*TTKpvdzyNXfPBhVyRqD6EA.png" alt="drawing"/>

## Todo :

 - Use Kotlin Flow 
 - Migrate to Dagger Hilt


# BoxJava
Using the BoxJava SDK, create a folder, upload file and create a shared link

Usage:
com.nike.box.boxJava.Main client_id client_secret token refresh_token pathFileName

The token & refreshToken are first used to get the BoxAPIConnection.
After successful connection, a state.conf is created that holds the token & refresh_token in it.

I thought the api.restore would get a refreshed state if it needed it but all it does is get the serialized json file
so I had to refresh the state if needed.


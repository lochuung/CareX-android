package hcmute.edu.vn.loclinhvabao.carex.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import hcmute.edu.vn.loclinhvabao.carex.data.remote.ApiService;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    @Provides
    public static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://your-api-url.com/") // 🔥 thay bằng URL thật
//                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    public ApiService provideApiService() {
        return provideRetrofit().create(ApiService.class); // demo
    }
}

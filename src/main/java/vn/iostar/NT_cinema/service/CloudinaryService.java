package vn.iostar.NT_cinema.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mongodb.annotations.Sealed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    @Autowired
    Cloudinary cloudinary;

    public String uploadImage(List<MultipartFile> imageFile) throws IOException {
        if (imageFile == null) {
            throw new IllegalArgumentException("File is null. Please upload a valid file.");
        }
        StringBuilder urls = new StringBuilder();
        for (MultipartFile item : imageFile) {
            Map<String, String> params = ObjectUtils.asMap(
                    "folder", "Movie",
                    "resource_type", "image");
            Map uploadResult = cloudinary.uploader().upload(item.getBytes(), params);
            String url = (String) uploadResult.get("secure_url");
            urls.append(url).append(";");
        }
        return urls.substring(0, urls.length() - 1);
    }

    public void deleteImage(String imageUrl) throws IOException {
        Map<String, String> params= ObjectUtils.asMap(
                "folder", "Movie",
                "resource_type", "image");
        Map result = cloudinary.uploader().destroy(getPublicIdImage(imageUrl), params);
        System.out.println(result.get("result").toString());

    }

    public String getPublicIdImage(String imageUrl)  {
        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
        String publicId = "Recruiment Assets/User/" + imageName;
        return publicId;
    }

}

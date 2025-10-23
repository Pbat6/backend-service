package com.the.repository;

import com.the.dto.response.UserDetailResponse;
import com.the.exception.InvalidDataException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.the.dto.response.PageResponse;
import com.the.model.User;
import com.the.dto.request.SearchCriteria;
import com.the.repository.criteria.UserSearchQueryCriteriaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.the.util.SearchConst.SEARCH_OPERATOR;
import static com.the.util.SearchConst.SORT_BY;

@Component
@Slf4j
public class SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String LIKE_FORMAT = "%%%s%%";
    private static final Pattern SEARCH_OPERATOR_PATTERN = Pattern.compile(SEARCH_OPERATOR);
    private static final Pattern SORT_BY_PATTERN = Pattern.compile(SORT_BY);

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "id",
            "firstName",
            "lastName",
            "phone",
            "email",
            "username",
            "roles"
            // Thêm các trường khác
    );

    /**
     * Advance search user by criterias
     *
     * @param offset
     * @param pageSize
     * @param sortBy
     * @param search
     * @return
     */
    public PageResponse<?> searchUserByCriteria(int offset, int pageSize, String sortBy, User actor, String... search) {
        log.info("Search user with search={} and sortBy={}", search, sortBy);

        List<SearchCriteria> criteriaList = new ArrayList<>();
        if (search.length > 0) {
            for (String s : search) {
                Matcher matcher = SEARCH_OPERATOR_PATTERN.matcher(s);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    // Kiểm tra key có hợp lệ không
                    if (!ALLOWED_FIELDS.contains(key)) {
                        throw new IllegalArgumentException("Trường tìm kiếm không hợp lệ: '" + key + "'");
                    }
                    if(key.equalsIgnoreCase("roles")){
                        boolean isActorAdmin = actor.getRoles().stream().anyMatch(uhr -> uhr.getRole().getName().equalsIgnoreCase("ADMIN"));
                        if(!isActorAdmin){
                            throw new InvalidDataException("chi admin moi co the tim kiem theo role");
                        }
                    }
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }else {
                    throw new IllegalArgumentException("search sai format or thua field");
                }
            }
        }

        List<User> users = getUsers(offset, pageSize, criteriaList, sortBy);
        List<UserDetailResponse> responses = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(uhr -> uhr.getRole().getName()).toList())
                .username(user.getUsername())
                .build()).toList();

        Long totalElements = getTotalElements(criteriaList);

        Page<User> page = new PageImpl<>(users, PageRequest.of(offset, pageSize), totalElements);

        return PageResponse.<List<UserDetailResponse>>builder()
                .pageNo(offset)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalItem(page.getTotalElements())
                .items(responses)
                .build();
    }

    /**
     * Get all users with conditions
     *
     * @param offset
     * @param pageSize
     * @param criteriaList
     * @param sortBy
     * @return
     */
    private List<User> getUsers(int offset, int pageSize, List<SearchCriteria> criteriaList, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); // Giúp tạo Query
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class); // Định nghĩa kiểu dữ liệu muốn trả về
        Root<User> userRoot = query.from(User.class); // Tương đương với from User u trong sql

        Predicate userPredicate = createPredicateForUserSearch(criteriaBuilder, userRoot, criteriaList); // Tương đương điều kiện where
        query.where(userPredicate);

        if (StringUtils.hasLength(sortBy)) {
            Matcher matcher = SORT_BY_PATTERN.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String direction = matcher.group(3);
                if (!ALLOWED_FIELDS.contains(fieldName)) {
                    throw new IllegalArgumentException("Trường sắp xếp không hợp lệ: '" + fieldName + "'");
                }
                if (direction.equalsIgnoreCase("asc")) {
                    query.orderBy(criteriaBuilder.asc(userRoot.get(fieldName)));
                } else {
                    query.orderBy(criteriaBuilder.desc(userRoot.get(fieldName)));
                }
            }else{
                throw new IllegalArgumentException("sort by sai format");
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    /**
     * Count users with conditions
     *
     * @param params
     * @return
     */
    private Long getTotalElements(List<SearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = createPredicateForUserSearch(criteriaBuilder, root, params);
        query.select(criteriaBuilder.count(root));
        query.where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    // Phương thức chung để tạo Predicate
    private Predicate createPredicateForUserSearch(CriteriaBuilder cb, Root<User> root, List<SearchCriteria> criteriaList) {
        Predicate predicate = cb.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(predicate, cb, root);
        criteriaList.forEach(searchConsumer);
        return searchConsumer.getPredicate(); // Lấy ra điều kiện đã được xây dựng từ params.forEach(searchConsumer);
    }
}

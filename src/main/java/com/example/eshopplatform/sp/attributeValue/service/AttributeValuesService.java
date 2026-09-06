package com.example.eshopplatform.sp.attributeValue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.sp.attributeValue.mapper.AttributeValuesMapper;
import com.example.eshopplatform.sp.attributeValue.entity.AttributeValues;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesCreateReq;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesUpdateReq;
import com.example.eshopplatform.sp.attributeValue.dto.AttributeValuesVO;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 属性值字典表服务
 * </p>
 *
 * <p>工程约定：Service 为具体类（不生成接口与 *ServiceImpl），直接注入 Mapper；
 * 入参/出参走 DTO（CreateReq/UpdateReq/VO），entity↔dto 的 toVO/apply 由生成器自动补齐。
 * 时间列在 VO 中以 Long（epoch 毫秒）返回；写操作只落业务字段，
 * created_at/updated_at/deleted_at 等自动列一律不进 apply。
 * 语义约定：update 为 DTO 覆盖（null 字段保留原值，非全量重置）；delete 默认逻辑删除
 * （表含 deleted_at 时实体自动标 @TableLogic，无该列的表才是物理删除）。
 * 以下为基础 CRUD，接入真实业务时按需加查询条件、校验与权限逻辑。</p>
 *
 * @since 2026-09-06
 */
@Service
@RequiredArgsConstructor
public class AttributeValuesService {

    /** 数据访问层 */
    private final AttributeValuesMapper attributeValuesMapper;

    /** 分页查询（第 page 页，每页 size 条；attributeId 缺省/null/0 均表示不筛属性） */
    public PageResult<AttributeValuesVO> page(int page, int size, Long attributeId) {
        Page<AttributeValues> p = new Page<>(normalizePage(page), normalizeSize(size));
        // null 短路避免拆箱 NPE；0 作为"不过滤"哨兵值
        boolean filterByAttribute = attributeId != null && attributeId != 0L;
        LambdaQueryWrapper<AttributeValues> wrapper = new LambdaQueryWrapper<AttributeValues>()
                .eq(filterByAttribute, AttributeValues::getAttributeId, attributeId)
                .orderByDesc(AttributeValues::getId);
        attributeValuesMapper.selectPage(p, wrapper);
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
    }

    /**
     * 按属性ID取启用属性值列表（对齐 gf ListByAttr，供 GET /api/v1/attributes/{id}/values）：
     * status=1，search_weight 降序、sort_order 升序。
     */
    public List<AttributeValuesVO> listByAttribute(Long attributeId) {
        List<AttributeValues> list = attributeValuesMapper.selectList(new LambdaQueryWrapper<AttributeValues>()
                .eq(AttributeValues::getAttributeId, attributeId)
                .eq(AttributeValues::getStatus, (byte) 1)
                .orderByDesc(AttributeValues::getSearchWeight)
                .orderByAsc(AttributeValues::getSortOrder));
        return list.stream().map(this::toVO).toList();
    }

    /*
     * 带筛选分页写法约定（本模板只生成无筛选版本；接入业务筛选时给 page 补筛选参数，
     * 推荐 MyBatis-Plus condition 链式写法：一行一个筛选条件，判定写在 condition 内，
     * false 自动跳过、无需 if 包裹，排序与条件同处一个查询描述）：
     *
     *     public PageResult<XxxVO> page(int page, int size, Integer status, String name) {
     *         Page<Xxx> p = new Page<>(normalizePage(page), normalizeSize(size));
     *         LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<Xxx>()
     *                 .eq(status != null, Xxx::getStatus, status)
     *                 .like(StringUtils.hasText(name), Xxx::getName, name)
     *                 .orderByDesc(Xxx::getId);
     *         xxxMapper.selectPage(p, wrapper);
     *         return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
     *     }
     *
     * 带业务语义的判定（如 parentId > 0 表示只筛子级、level > 0）也写在 condition 里；
     * 判定一长就读不动时，抽成带名字的局部 boolean 或单独 if，不要硬塞链式。
     */

    /** 按主键查询 */
    public AttributeValuesVO getById(Long id) {
        return toVO(require(id));
    }

    /** 新增 */
    public AttributeValuesVO create(AttributeValuesCreateReq req) {
        AttributeValues entity = new AttributeValues();
        apply(entity, req);
        attributeValuesMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 按主键更新（DTO 覆盖语义）。
     * 注意：MyBatis-Plus 默认 NOT_NULL 策略——req 中为 null 的字段不会生成 SET，
     * 即"没传的字段保留原值"；若业务要求"传 null = 重置为默认值"，请在 apply 内
     * 对该字段显式兜底（如 entity.setStatus(req.getStatus() == null ? 1 : req.getStatus())）。
     */
    public AttributeValuesVO update(Long id, AttributeValuesUpdateReq req) {
        AttributeValues entity = require(id);
        apply(entity, req);
        attributeValuesMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 按主键删除。
     * 注意：sp_attribute_values 表无 deleted_at 列（值字典属字典/流水型数据），
     * 实体未标 @TableLogic，deleteById 为物理删除；被商品/SKU 引用删除前请自行确认。
     * 业务上若存在"被引用 / 子级存在时不允许删除"等约束，接入时先查后删并抛
     * {@code BizException.conflict}，例如：
     * <pre>{@code
     * long ref = xxxMapper.selectCount(new LambdaQueryWrapper<Xxx>()
     *         .eq(Xxx::getCategoryId, id));
     * if (ref > 0) {
     *     throw BizException.conflict("该记录已被引用，无法删除");
     * }
     * }</pre>
     */
    public void delete(Long id) {
        require(id);
        attributeValuesMapper.deleteById(id);
    }

    /** 按主键查询，不存在抛 404 */
    private AttributeValues require(Long id) {
        AttributeValues entity = attributeValuesMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("属性值字典表不存在");
        }
        return entity;
    }

    /** entity -> VO（生成器自动映射：deleted_at 不对外暴露；时间列转 Long epoch 毫秒） */
    private AttributeValuesVO toVO(AttributeValues entity) {
        AttributeValuesVO vo = new AttributeValuesVO();
        vo.setId(entity.getId());
        vo.setAttributeId(entity.getAttributeId());
        vo.setValue(entity.getValue());
        vo.setAlias(entity.getAlias());
        vo.setSearchWeight(entity.getSearchWeight());
        vo.setNumericValue(entity.getNumericValue());
        vo.setColorHex(entity.getColorHex());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(TimeUtil.toEpochMillis(entity.getCreatedAt()));
        return vo;
    }

    /** CreateReq -> entity（生成器自动映射：主键 id、created_at/updated_at/deleted_at 等
     *  自动列绝不在此赋值；业务字段计算在此补充） */
    private void apply(AttributeValues entity, AttributeValuesCreateReq req) {
        entity.setAttributeId(req.getAttributeId());
        entity.setValue(req.getValue());
        entity.setAlias(req.getAlias());
        entity.setSearchWeight(req.getSearchWeight());
        entity.setNumericValue(req.getNumericValue());
        entity.setColorHex(req.getColorHex());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }

    /** UpdateReq -> entity（同上：全量更新时只落业务字段） */
    private void apply(AttributeValues entity, AttributeValuesUpdateReq req) {
        entity.setAttributeId(req.getAttributeId());
        entity.setValue(req.getValue());
        entity.setAlias(req.getAlias());
        entity.setSearchWeight(req.getSearchWeight());
        entity.setNumericValue(req.getNumericValue());
        entity.setColorHex(req.getColorHex());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
    }

    /** 页号归一化：至少从第 1 页开始 */
    static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    /** 每页条数归一化：1~1000 */
    static int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 1000);
    }
}

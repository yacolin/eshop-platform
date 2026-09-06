package com.example.eshopplatform.sp.sku.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eshopplatform.sp.sku.mapper.SkusMapper;
import com.example.eshopplatform.sp.sku.entity.Skus;
import com.example.eshopplatform.sp.sku.dto.SkusCreateReq;
import com.example.eshopplatform.sp.sku.dto.SkusUpdateReq;
import com.example.eshopplatform.sp.sku.dto.SkusVO;
import com.example.eshopplatform.common.BizException;
import com.example.eshopplatform.common.PageResult;
import com.example.eshopplatform.common.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * SKU规格表（具体可售单元）服务
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
public class SkusService {

    /** 数据访问层 */
    private final SkusMapper skusMapper;

    /** 分页查询（第 page 页，每页 size 条） */
    public PageResult<SkusVO> page(int page, int size, Long productId) {
        Page<Skus> p = new Page<>(normalizePage(page), normalizeSize(size));
        skusMapper.selectPage(p, new LambdaQueryWrapper<Skus>()
                .eq(productId != null && productId > 0, Skus::getProductId, productId)
                .orderByDesc(Skus::getId));
        return PageResult.of(p.getTotal(), p.getRecords().stream().map(this::toVO).toList());
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
    public SkusVO getById(Long id) {
        return toVO(require(id));
    }

    /** 按SKU编码查询 */
    public SkusVO getBySkuCode(String skuCode) {
        Skus entity = skusMapper.selectOne(new LambdaQueryWrapper<Skus>()
                .eq(Skus::getSkuCode, skuCode));
        if (entity == null) {
            throw BizException.notFound("SKU规格表（具体可售单元）不存在");
        }
        return toVO(entity);
    }

    /** 新增 */
    public SkusVO create(SkusCreateReq req) {
        Skus entity = new Skus();
        apply(entity, req);
        skusMapper.insert(entity);
        return toVO(entity);
    }

    /**
     * 按主键更新（DTO 覆盖语义）。
     * 注意：MyBatis-Plus 默认 NOT_NULL 策略——req 中为 null 的字段不会生成 SET，
     * 即"没传的字段保留原值"；若业务要求"传 null = 重置为默认值"，请在 apply 内
     * 对该字段显式兜底（如 entity.setStatus(req.getStatus() == null ? 1 : req.getStatus())）。
     */
    public SkusVO update(Long id, SkusUpdateReq req) {
        Skus entity = require(id);
        apply(entity, req);
        skusMapper.updateById(entity);
        return toVO(entity);
    }

    /**
     * 按主键删除（默认逻辑删除）。
     * 表含 deleted_at 列时，实体该字段已标 {@code @TableLogic(value = "null", delval = "now()")}：
     * deleteById 自动转 {@code UPDATE ... SET deleted_at = now()}，且普通查询
     * （selectById/page/wrapper）自动带 {@code deleted_at IS NULL}，无需手写过滤；
     * 仅当表本身没有 deleted_at 列（流水/记录表）时才是物理删除。
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
        skusMapper.deleteById(id);
    }

    /** 按主键查询，不存在抛 404 */
    private Skus require(Long id) {
        Skus entity = skusMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("SKU规格表（具体可售单元）不存在");
        }
        return entity;
    }

    /** entity -> VO（生成器自动映射：deleted_at 不对外暴露；时间列转 Long epoch 毫秒） */
    private SkusVO toVO(Skus entity) {
        SkusVO vo = new SkusVO();
        vo.setId(entity.getId());
        vo.setProductId(entity.getProductId());
        vo.setMerchantId(entity.getMerchantId());
        vo.setSkuCode(entity.getSkuCode());
        vo.setBarcode(entity.getBarcode());
        vo.setSpecSummary(entity.getSpecSummary());
        vo.setPrice(entity.getPrice());
        vo.setMarketPrice(entity.getMarketPrice());
        vo.setCostPrice(entity.getCostPrice());
        vo.setWeight(entity.getWeight());
        vo.setVolume(entity.getVolume());
        vo.setLength(entity.getLength());
        vo.setWidth(entity.getWidth());
        vo.setHeight(entity.getHeight());
        vo.setMinPurchaseQty(entity.getMinPurchaseQty());
        vo.setMaxPurchaseQty(entity.getMaxPurchaseQty());
        vo.setImage(entity.getImage());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(TimeUtil.toEpochMillis(entity.getCreatedAt()));
        vo.setUpdatedAt(TimeUtil.toEpochMillis(entity.getUpdatedAt()));
        return vo;
    }

    /** CreateReq -> entity（生成器自动映射：主键 id、created_at/updated_at/deleted_at 等
     *  自动列绝不在此赋值；业务字段计算在此补充） */
    private void apply(Skus entity, SkusCreateReq req) {
        entity.setProductId(req.getProductId());
        entity.setMerchantId(req.getMerchantId());
        entity.setSkuCode(req.getSkuCode());
        entity.setBarcode(req.getBarcode());
        entity.setSpecSummary(req.getSpecSummary());
        entity.setPrice(req.getPrice());
        entity.setMarketPrice(req.getMarketPrice());
        entity.setCostPrice(req.getCostPrice());
        entity.setWeight(req.getWeight());
        entity.setVolume(req.getVolume());
        entity.setLength(req.getLength());
        entity.setWidth(req.getWidth());
        entity.setHeight(req.getHeight());
        entity.setMinPurchaseQty(req.getMinPurchaseQty());
        entity.setMaxPurchaseQty(req.getMaxPurchaseQty());
        entity.setImage(req.getImage());
        entity.setStatus(req.getStatus());
    }

    /** UpdateReq -> entity（同上：全量更新时只落业务字段） */
    private void apply(Skus entity, SkusUpdateReq req) {
        entity.setProductId(req.getProductId());
        entity.setMerchantId(req.getMerchantId());
        entity.setSkuCode(req.getSkuCode());
        entity.setBarcode(req.getBarcode());
        entity.setSpecSummary(req.getSpecSummary());
        entity.setPrice(req.getPrice());
        entity.setMarketPrice(req.getMarketPrice());
        entity.setCostPrice(req.getCostPrice());
        entity.setWeight(req.getWeight());
        entity.setVolume(req.getVolume());
        entity.setLength(req.getLength());
        entity.setWidth(req.getWidth());
        entity.setHeight(req.getHeight());
        entity.setMinPurchaseQty(req.getMinPurchaseQty());
        entity.setMaxPurchaseQty(req.getMaxPurchaseQty());
        entity.setImage(req.getImage());
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

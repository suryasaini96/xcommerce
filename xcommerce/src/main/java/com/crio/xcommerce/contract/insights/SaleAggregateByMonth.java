
package com.crio.xcommerce.contract.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SaleAggregateByMonth {

  private Integer month;
  private Double sales;
}


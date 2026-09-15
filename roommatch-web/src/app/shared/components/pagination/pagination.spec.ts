import { Pagination } from './pagination';

describe('Pagination boundaries', () => {
  it('shows a bounded window and prevents invalid or duplicate navigation', () => {
    const component=new Pagination(); component.totalPages=100; component.page=50;
    expect(component.pages).toEqual([48,49,50,51,52]);
    const emit=vi.spyOn(component.pageChange,'emit');
    component.go(-1); component.go(100); component.go(50);
    expect(emit).not.toHaveBeenCalled();
    component.go(49); expect(emit).toHaveBeenCalledWith(49);
    component.loading=true; component.go(48); expect(emit).toHaveBeenCalledTimes(1);
    component.page=99; expect(component.pages).toEqual([95,96,97,98,99]);
  });
  it('does not create phantom pages for empty or malformed counts', () => {
    const component=new Pagination();
    for (const total of [0,-1,NaN,Infinity]) {component.totalPages=total;expect(component.pages).toEqual([]);}
  });
});
